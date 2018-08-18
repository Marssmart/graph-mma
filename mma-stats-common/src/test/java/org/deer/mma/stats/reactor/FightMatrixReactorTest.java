package org.deer.mma.stats.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.db.repository.FighterRepo;
import org.deer.mma.stats.db.repository.RepositoryTest;
import org.deer.mma.stats.reactor.LinkResolverReactor.DiscoverySession;
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

    final Collection<String> values = sessionInfo.getDiscoveredLinksPerNameIndex().values();

    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Paulo+Henrique+Costa/121995/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Demian+Maia/26541/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Marcio+Alexandre+Jr./93602/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Darren+Till/75162/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Yoel+Romero/61978/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Stephen+Thompson/60803/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Tyron+Woodley/41178/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Robert+Whittaker/45036/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Rory+MacDonald/25924/"));
  }

  @Test(timeout = 20000)
  public void resolveLinksSomeExisting() {
    fighterRepo.save(new Fighter().setFullname("Uriah Hall"));

    DiscoverySession sessionInfo = reactor
        .extractNewFighters("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/")
        .join();
    Collection<String> discoveredLinks = sessionInfo.getDiscoveredLinksPerNameIndex().values();
    assertEquals(10, discoveredLinks.size());

    final Collection<String> values = sessionInfo.getDiscoveredLinksPerNameIndex().values();

    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Paulo+Henrique+Costa/121995/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Demian+Maia/26541/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Marcio+Alexandre+Jr./93602/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Darren+Till/75162/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Yoel+Romero/61978/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Stephen+Thompson/60803/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Tyron+Woodley/41178/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Robert+Whittaker/45036/"));
    assertTrue(values
        .contains("http://www.fightmatrix.com/fighter-profile/Rory+MacDonald/25924/"));
  }
}