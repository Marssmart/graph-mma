package org.deer.mma.stats.db.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import org.deer.mma.stats.db.node.Fighter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FighterRepoTest extends RepositoryTest {

  @Autowired
  private FighterRepo fighterRepo;

  @Test
  public void getAllFighterNames() {
    fighterRepo.saveAll(Arrays.asList(new Fighter().setFullname("Connor McGregor"),
        new Fighter().setFullname("Chad Mendes"),
        new Fighter().setFullname("Uriah Faber")));

    Set<String> allFighterNames = fighterRepo.getAllFighterNames();
    assertEquals(3, allFighterNames.size());
    assertTrue(allFighterNames.contains("Connor McGregor"));
    assertTrue(allFighterNames.contains("Chad Mendes"));
    assertTrue(allFighterNames.contains("Uriah Faber"));
  }
}