package org.deer.mma.stats.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.db.repository.FighterRepo;
import org.deer.mma.stats.db.repository.RepositoryTest;
import org.deer.mma.stats.reactor.LinkResolverReactor.DiscoverySession;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FightMatrixReactorTest extends RepositoryTest {

  @Autowired
  private FightMatrixReactor reactor;

  @Autowired
  private FighterRepo fighterRepo;

  @Test(timeout = 20000)
  public void resolveLinksNoAllreadyExisting() {
    DiscoverySession sessionInfo = reactor
        .extractNewFighters("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/")
        .join();
    assertEquals(10, sessionInfo.getDiscoveredLinksPerNameIndex().size());
  }

  @Test(timeout = 20000)
  public void resolveLinksSomeExisting() {
    fighterRepo.save(new Fighter().setFullname("Uriah Hall"));

    DiscoverySession sessionInfo = reactor
        .extractNewFighters("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/")
        .join();
    Collection<String> discoveredLinks = sessionInfo.getDiscoveredLinksPerNameIndex().values();
    assertEquals(10, discoveredLinks.size());
    assertFalse(discoveredLinks
        .contains("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/"));
  }

  @Test
  public void parseFighterLinks() throws IOException {
    URL resource = Resources.getResource("fight-matrix-example-payload");
    String content = Resources.toString(resource, StandardCharsets.UTF_8);

    Set<String> set = FightMatrixReactor.parseFighterLinks(content);

    assertEquals(3, set.size());
    assertTrue(
        set.contains("http://www.fightmatrix.com/fighter-profile/Rory+MacDonald/25924/"));
    assertTrue(set.contains("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/"));
    assertTrue(
        set.contains("http://www.fightmatrix.com/fighter-profile/Rafael+Carvalho/90597/"));
  }

  @Test
  public void parseFighterName() {
    Optional<String> rory = FightMatrixReactor
        .parseFullName("http://www.fightmatrix.com/fighter-profile/Rory+MacDonald/25924/");
    Optional<String> uriah = FightMatrixReactor
        .parseFullName("http://www.fightmatrix.com/fighter-profile/Uriah/26116/");
    Optional<String> rafael = FightMatrixReactor
        .parseFullName("http://www.fightmatrix.com/fighter-profile/Rafael+Carvalho+Monina/90597/");

    assertEquals("Rory MacDonald", rory.get());
    assertEquals("Uriah", uriah.get());
    assertEquals("Rafael Carvalho Monina", rafael.get());
  }
}