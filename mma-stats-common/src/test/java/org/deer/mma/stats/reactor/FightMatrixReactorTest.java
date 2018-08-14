package org.deer.mma.stats.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deer.mma.stats.reactor.LinkResolverReactor.DiscoverySessionInfo;
import org.junit.Test;

public class FightMatrixReactorTest {

  @Test
  public void resolveLinks() {
    FightMatrixReactor reactor = new FightMatrixReactor(
        "http://www.fightmatrix.com/fighter-profile/Gegard+Mousasi/13436/",
        new DefaultHttpClient(), 10);

    DiscoverySessionInfo sessionInfo = reactor.extractNewFighters().join();
    assertEquals(10, sessionInfo.getDiscoveredLinks().size());
  }

  @Test
  public void parseFighterLinks() throws IOException {
    URL resource = Resources.getResource("fight-matrix-example-payload");
    String content = Resources.toString(resource, StandardCharsets.UTF_8);

    Set<String> set = FightMatrixReactor.parseFighterLinks(content);

    assertEquals(3, set.size());
    assertTrue(set.contains("http://www.fightmatrix.com/fighter-profile/Rory+MacDonald/25924/"));
    assertTrue(set.contains("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/"));
    assertTrue(set.contains("http://www.fightmatrix.com/fighter-profile/Rafael+Carvalho/90597/"));
  }
}