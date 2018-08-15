package org.deer.mma.stats.reactor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.neo4j.graphdb.Node;

public interface LinkResolverReactor {

  /**
   * Extract links of fighters not already existing in database
   */
  CompletableFuture<DiscoverySession> extractNewFighters(
      @Nonnull final String startingPointLink);

  void decorateFighterByLink(final Node fighter, final String link);

  final class DiscoverySession {

    private final String originalLink;
    private final Map<String, String> discoveredLinksPerNameIndex;
    private final BiConsumer<Node, String> linkApplier;

    public DiscoverySession(String originalLink,
        Map<String, String> discoveredLinksPerNameIndex,
        BiConsumer<Node, String> linkApplier) {
      this.originalLink = originalLink;

      this.discoveredLinksPerNameIndex = discoveredLinksPerNameIndex;
      this.linkApplier = linkApplier;
    }

    public BiConsumer<Node, String> getLinkApplier() {
      return linkApplier;
    }

    public String getOriginalLink() {
      return originalLink;
    }

    public Map<String, String> getDiscoveredLinksPerNameIndex() {
      return discoveredLinksPerNameIndex;
    }

    @Override
    public String toString() {
      return "DiscoverySession(originalLink="
          + originalLink
          + ", indexSize="
          + discoveredLinksPerNameIndex.size()
          + ")";
    }
  }
}
