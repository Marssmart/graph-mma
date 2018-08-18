package org.deer.mma.stats.reactor;

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.reactor.request.HtmlPageRequester;
import org.jsoup.Jsoup;
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

  @Override
  public CompletableFuture<DiscoverySession> extractNewFighters(
      @Nonnull final String startingPointLink) {
    LOG.info("Starting new fighters extraction for {}, limit {}", startingPointLink, limit);
    return CompletableFuture.supplyAsync(() -> {
      final AtomicInteger limitCounter = new AtomicInteger(limit);

      //links that are directly on starting point page
      final Map<String, String> discoveredLinksIndexPerFighterName = new HashMap<>();

      final CompletableFuture<Optional<String>> contentFuture = basicHttpRequester
          .requestLink(startingPointLink);

      final List<String> discoveredLinks = Stream.concat(
          collectLinksWithinPage(contentFuture).join().stream(),
          Stream.of(startingPointLink))
          .filter(link -> {
            Optional<String> parsedName = parseFullName(link);
            if (parsedName.isPresent()) {
              if (limitCounter.getAndDecrement() > 0) {
                discoveredLinksIndexPerFighterName.put(parsedName.get(), link);
              }
              return true;
            }
            return false;
          }).collect(Collectors.toCollection(LinkedList::new));

      //little trick so i don't have to use recursion
      final ListIterator<String> growingIterator = discoveredLinks.listIterator();
      while (growingIterator.hasNext() && limitCounter.get() > 0) {
        //collect links withing next link in line and than add them at the end of iterator
        String nextLink = growingIterator.next();
        collectLinksWithinPage(basicHttpRequester.requestLink(nextLink))
            .join()
            .stream()
            .filter(link -> !discoveredLinks.contains(link))//omit duplicates
            .filter(link -> !link.equals(startingPointLink))//to prevent loops
            .forEach(link -> {
              final Optional<String> parsedName = parseFullName(link);

              if (parsedName.isPresent()) {

                if (limitCounter.getAndDecrement() > 0) {
                  discoveredLinksIndexPerFighterName.put(parsedName.get(), link);
                  growingIterator.add(link);
                  //ListIterator#add shifts the nextIndex so it does match the element
                  //that should be returned next, by calling ListIterator#previous,
                  //we can omit skipping the processing of newly added elements,therefore
                  //this method can replace the reflection calling here and also can be debugged easily
                  growingIterator.previous();
                }
              }
            });
      }

      final String sherdogLink = parseSherdogLink(contentFuture.join().orElse(""))
          .orElse("N/A");

      return new DiscoverySession(startingPointLink,
          sherdogLink, discoveredLinksIndexPerFighterName,
          this::decorateFighterByLink);
    });
  }

  @Override
  public Fighter decorateFighterByLink(Fighter fighter, String link) {
    return fighter.setFightMatrixLink(link);
  }

  //TODO rewrite to jsoup
  static Set<String> parseFightMatrixLinks(String content) {
    return Splitter.on(HREF_FIGHTER_PROFILE_MASK)
        .splitToList(content)
        .stream()
        .skip(1)
        .map(s -> s.substring(0, s.indexOf("'")))
        .map(s -> HREF_PROFILE_BASE + s)
        .map(String::trim)
        .collect(Collectors.toSet());
  }

  static Optional<String> parseSherdogLink(String content) {
    return Optional.ofNullable(Jsoup.parse(content)
        .select("a:has(img)[target='_blank']")
        .attr("href"))
        .map(s -> s.isEmpty() ? null : s);
  }

  //http://www.fightmatrix.com/fighter-profile/Gegard+Mousasi/13436/
  static Optional<String> parseFullName(String link) {
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

  private CompletableFuture<Set<String>> collectLinksWithinPage(
      CompletableFuture<Optional<String>> contentFuture) {
    return contentFuture.thenApply(content -> content.isPresent() ?
        parseFightMatrixLinks(content.get()) :
        Collections.emptySet());
  }
}
