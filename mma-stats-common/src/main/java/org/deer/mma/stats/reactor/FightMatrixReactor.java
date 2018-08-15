package org.deer.mma.stats.reactor;

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.deer.mma.stats.db.EmbeddedDbService;
import org.deer.mma.stats.db.nodes.Fighter;
import org.deer.mma.stats.reactor.request.HtmlPageRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fight-matrix-reactor")
public class FightMatrixReactor implements LinkResolverReactor {

  private static final Logger LOG = LoggerFactory.getLogger(FightMatrixReactor.class);

  private static final String FIGHTER_PROFILE_LINK_BASE = "/fighter-profile/";
  private static final String HREF_FIGHTER_PROFILE_MASK = "href='" + FIGHTER_PROFILE_LINK_BASE;
  private static final String HREF_PROFILE_BASE =
      "http://www.fightmatrix.com" + FIGHTER_PROFILE_LINK_BASE;

  @Value("${fight.matrix.link.limit}")
  private int limit;

  @Autowired
  @Qualifier("basic")
  private HtmlPageRequester basicHttpRequester;

  @Autowired
  private EmbeddedDbService dbService;

  @Override
  public CompletableFuture<DiscoverySessionInfo> extractNewFighters(
      @Nonnull final String startingPointLink) {
    return CompletableFuture.supplyAsync(() -> {

      final Set<String> existingFighterNames = dbService.doInTxAndReturnOptional(() ->
          dbService.findAllFighterNodes()
              .stream()
              .map(node -> node.getProperty(Fighter.FULLNAME))
              .map(String.class::cast)
              .collect(Collectors.toSet())).orElse(Collections.emptySet());

      //links that are directly on starting point page
      final List<String> discoveredLinks = collectLinksWithinPage(startingPointLink).join().stream()
          .filter(link -> {
            Optional<String> parsedLink = parseFullName(link);
            if (!parsedLink.isPresent()) {
              LOG.warn("Link not parseable {}", parsedLink);
              return false;
            }

            return !existingFighterNames.contains(parsedLink.get());
          }).collect(Collectors.toCollection(LinkedList::new));

      //little trick so i don't have to use recursion
      final ListIterator<String> growingIterator = discoveredLinks.listIterator();
      while (growingIterator.hasNext() && discoveredLinks.size() < limit) {
        //collect links withing next link in line and than add them at the end of iterator
        String nextLink = growingIterator.next();
        collectLinksWithinPage(nextLink)
            .join()
            .stream()
            .filter(link -> !discoveredLinks.contains(link))//omit duplicates
            .filter(link -> !link.equals(startingPointLink))//to prevent loops
            .forEach(link -> {
              Optional<String> parsedLink = parseFullName(link);
              if (!parsedLink.isPresent()) {
                LOG.warn("Link not parseable {}", parsedLink);
                return;
              }

              if (existingFighterNames.contains(parsedLink.get())) {
                //allready existing
                return;
              }

              growingIterator.add(link);
              //ListIterator#add shifts the nextIndex so it does match the element
              //that should be returned next, by calling ListIterator#previous,
              //we can omit skipping the processing of newly added elements,therefore
              //this method can replace the reflection calling here and also can be debugged easily
              growingIterator.previous();
            });
      }

      return new DiscoverySessionInfo(discoveredLinks.stream()
          .limit(limit)
          .collect(Collectors.toList()));
    });
  }

  private CompletableFuture<Set<String>> collectLinksWithinPage(final String link) {
    return basicHttpRequester.requestLink(link)
        .thenApply(content -> content.isPresent() ?
            parseFighterLinks(content.get()) :
            Collections.emptySet());
  }

  public static Set<String> parseFighterLinks(String content) {
    return Splitter.on(HREF_FIGHTER_PROFILE_MASK)
        .splitToList(content)
        .stream()
        .skip(1)
        .map(s -> s.substring(0, s.indexOf("'")))
        .map(s -> HREF_PROFILE_BASE + s)
        .map(String::trim)
        .collect(Collectors.toSet());
  }

  //http://www.fightmatrix.com/fighter-profile/Gegard+Mousasi/13436/
  public static Optional<String> parseFullName(String link) {
    int hrefStart = link.indexOf(FIGHTER_PROFILE_LINK_BASE);

    if (hrefStart == -1) {
      LOG.warn("{} not found in link {}", link, FIGHTER_PROFILE_LINK_BASE);
      return Optional.empty();
    }

    int slashAfterNameStart = link.indexOf('/', hrefStart + FIGHTER_PROFILE_LINK_BASE.length());

    if (slashAfterNameStart == -1) {
      LOG.warn("Slash not found in link {} after fighter name", link, FIGHTER_PROFILE_LINK_BASE);
      return Optional.empty();
    }

    return Optional.of(
        link.substring(hrefStart + FIGHTER_PROFILE_LINK_BASE.length(), slashAfterNameStart)
            .replace("+", " "));
  }
}
