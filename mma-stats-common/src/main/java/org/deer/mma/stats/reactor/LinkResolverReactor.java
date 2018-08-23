package org.deer.mma.stats.reactor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import org.deer.mma.stats.db.node.Fighter;

public interface LinkResolverReactor {

  /**
   * Extract links of fighters not already existing in database
   */
  CompletableFuture<DiscoverySession> extractFighterLinks(
      @Nonnull final String startingPointLink);

  CompletableFuture<Map<String, String>> discoverAdditionalAttributes(@Nonnull final String link);

  Fighter decorateFighterByLink(final Fighter fighter, final String link);

  Fighter decorateFighterByAdditionalAttributes(final Fighter fighter,
      final Map<String, String> attributes);

  final class DiscoverySession {

    private final String originalLink;
    private final Map<String, String> discoveredLinksPerNameIndex;
    private final BiFunction<Fighter, String, Fighter> linkApplier;

    public DiscoverySession(String originalLink, Map<String, String> discoveredLinksPerNameIndex,
        BiFunction<Fighter, String, Fighter> linkApplier) {
      this.originalLink = originalLink;
      this.discoveredLinksPerNameIndex = discoveredLinksPerNameIndex;
      this.linkApplier = linkApplier;
    }

    public Fighter applyLink(final Fighter fighter, final String link) {
      return linkApplier.apply(fighter, link);
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
