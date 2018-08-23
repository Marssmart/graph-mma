package org.deer.mma.stats.db.repository;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.deer.mma.stats.db.node.enumerated.FightEndType.KO;
import static org.deer.mma.stats.db.node.enumerated.FightEndType.SUBMISSION;
import static org.deer.mma.stats.db.node.enumerated.FightEndType.TKO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import org.deer.mma.stats.db.node.Event;
import org.deer.mma.stats.db.node.Fight;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.db.node.Referee;
import org.deer.mma.stats.db.node.enumerated.FightEndType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FightRepoTest extends RepositoryTest {

  @Autowired
  private EventRepo eventRepo;

  @Autowired
  private RefereeRepo refereeRepo;

  @Autowired
  private FighterRepo fighterRepo;

  @Autowired
  private FightRepo fightRepo;

  private static Fighter newFighter(final String name, final Set<Fight> wins,
      final Set<Fight> losses) {
    return new Fighter().setFullname(name).setWins(wins).setLosses(losses);
  }

  private static Fight newFight(int rounds, int durationInMinutes, Referee referee, FightEndType endType) {
    return new Fight().setNumberOfRounds((byte) rounds)
        .setStoppageTime(Duration.ofMinutes(durationInMinutes))
        .setFightEndType(endType)
        .setReferee(referee);
  }

  @Test
  public void testMatchFight() {

    final Referee johnMcCarthy = refereeRepo.save(new Referee().setName("John McCarthy"));

    final Fight connorVsChad = fightRepo.save(newFight(4, 2, johnMcCarthy,KO));
    final Fight connorVsMax = fightRepo.save(newFight(2, 1, johnMcCarthy, TKO));
    final Fight maxVsAldo = fightRepo.save(newFight(3, 4, johnMcCarthy,SUBMISSION));

    eventRepo
        .save(new Event().setName("UFC 215").setFights(ImmutableSet.of(connorVsChad, maxVsAldo)));
    eventRepo.save(new Event().setName("UFC 114").setFights(ImmutableSet.of(connorVsMax)));

    final Fighter connorMcGregor = fighterRepo.save(newFighter("Connor McGregor",
        ImmutableSet.of(connorVsChad, connorVsMax), emptySet()));
    fighterRepo
        .save(newFighter("Chad Mendes", emptySet(), singleton(connorVsChad)));
    fighterRepo.save(newFighter("Max Holloway", singleton(maxVsAldo), emptySet()));
    fighterRepo.save(newFighter("Jose Aldo", emptySet(), singleton(maxVsAldo)));

    final Optional<Fight> fightsBetweenConnerAndChad = fightRepo
        .matchByFighterAndStopageTimeAndRound(connorMcGregor.getId(),connorVsChad.getFightEndType(),
            connorVsChad.getNumberOfRounds(), connorVsChad.getStoppageTimeConverted());
    assertTrue(fightsBetweenConnerAndChad.isPresent());
    assertEquals(connorVsChad, fightsBetweenConnerAndChad.get());
  }
}