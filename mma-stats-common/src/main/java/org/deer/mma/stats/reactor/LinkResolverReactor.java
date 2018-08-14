package org.deer.mma.stats.reactor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LinkResolverReactor {

  /**
   * Extract links of fighters not already existing in database
   */
  CompletableFuture<DiscoverySessionInfo> extractNewFighters();

  final class DiscoverySessionInfo {

    private final List<String> discoveredLinks;

    public DiscoverySessionInfo(List<String> discoveredLinks) {

      this.discoveredLinks = discoveredLinks;
    }

    public List<String> getDiscoveredLinks() {
      return discoveredLinks;
    }
  }
}
