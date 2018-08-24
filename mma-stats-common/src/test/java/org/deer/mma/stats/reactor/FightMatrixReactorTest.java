package org.deer.mma.stats.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        .extractFighterLinks("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/")
        .join();
    assertEquals(10, sessionInfo.getDiscoveredLinksPerNameIndex().size());

    assertFalse(sessionInfo.getDiscoveredLinksPerNameIndex().isEmpty());
  }
}