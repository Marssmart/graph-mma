package org.deer.mma.stats.reactor;

import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.http.client.HttpClient;
import org.deer.mma.stats.reactor.parser.BasicHttpRequester;

public class FightMatrixReactor implements LinkResolverReactor {

  private static final String HREF_FIGHTER_PROFILE_MASK = "href='/fighter-profile/";
  private static final String HREF_PROFILE_BASE = "http://www.fightmatrix.com/fighter-profile/";

  private final String startingPointLink;
  private final BasicHttpRequester basicHttpRequester;
  private final int limit;

  FightMatrixReactor(@Nonnull final String startingPointLink,
      @Nonnull final HttpClient httpClient,
      @Nonnegative final int limit) {
    this.startingPointLink = startingPointLink;
    basicHttpRequester = new BasicHttpRequester(httpClient);
    this.limit = limit;
  }

  @Override
  public CompletableFuture<DiscoverySessionInfo> extractNewFighters() {
    return CompletableFuture.supplyAsync(() -> {
      //TODO filter out links that already exist in db

      //links that are directly on starting point page
      Set<String> discoveredLOne = collectLinksWithinPage(startingPointLink).join();

      final List<String> discoveredLinks = new LinkedList<>(discoveredLOne);

      //little trick so i don't have to use recursion
      final ListIterator<String> growingIterator = discoveredLinks.listIterator();
      while (growingIterator.hasNext() && discoveredLinks.size() < limit) {
        //collect links withing next link in line and than add them at the end of iterator
        String nextLink = growingIterator.next();
        collectLinksWithinPage(nextLink)
            .join()
            .stream()
            .filter(link -> !discoveredLinks.contains(link))//omit duplicates
            .forEach(link -> {
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

  static Set<String> parseFighterLinks(String content) {
    return Splitter.on(HREF_FIGHTER_PROFILE_MASK)
        .splitToList(content)
        .stream()
        .skip(1)
        .map(String::trim)
        .map(s -> s.substring(0, s.indexOf("'")))
        .map(s -> HREF_PROFILE_BASE + s)
        .collect(Collectors.toSet());
  }
}
