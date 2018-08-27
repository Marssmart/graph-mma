package org.deer.mma.stats.db.repository;

import static org.deer.mma.stats.db.node.enumerated.FightEndType.DECISION_SPLIT;
import static org.deer.mma.stats.db.node.enumerated.FightEndType.KO;
import static org.deer.mma.stats.db.node.enumerated.FightEndType.SUBMISSION;
import static org.deer.mma.stats.db.node.enumerated.FightEndType.TKO;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.deer.mma.stats.db.node.Event;
import org.deer.mma.stats.db.node.Fight;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.db.node.projections.CountFightsByFighter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FightRepoTest extends RepositoryTest {

  @Autowired
  private FightRepo fightRepo;

  @Autowired
  private EventRepo eventRepo;

  @Autowired
  private FighterRepo fighterRepo;

  private Fight koFight;
  private Fight tkoFight;
  private Fight subFight;
  private Fight decFight;

  private Fighter fighterA;
  private Fighter fighterB;
  private Fighter fighterC;
  private Event eventOne;
  private Event eventTwo;
  private Event eventThree;

  @Before
  public void init() {
    koFight = fightRepo.save(new Fight().setFightEndType(KO));
    tkoFight = fightRepo.save(new Fight().setFightEndType(TKO));
    subFight = fightRepo.save(new Fight().setFightEndType(SUBMISSION));
    decFight = fightRepo.save(new Fight().setFightEndType(DECISION_SPLIT));

    eventOne = createEvent(7, 5, "UFC 1", ImmutableSet.of(koFight, tkoFight));
    eventTwo = createEvent(8, 4, "UFC 2", ImmutableSet.of(decFight));
    eventThree = createEvent(2, 7, "UFC 3", ImmutableSet.of(subFight));

    fighterA = createFighter("A", Collections.emptySet(), ImmutableSet.of(koFight));
    fighterB = createFighter("B", ImmutableSet.of(koFight, decFight),
        ImmutableSet.of(subFight, tkoFight));
    fighterC = createFighter("C", ImmutableSet.of(tkoFight, subFight), ImmutableSet.of(decFight));
  }

  @Test
  public void countAllByParticipant() {
    Map<Long, Long> counts = fightRepo.countAllByParticipant()
        .stream()
        .collect(Collectors.toMap(CountFightsByFighter::getFighterId,
            CountFightsByFighter::getCountOfFights));

    assertEquals(3, counts.size());
    assertEquals(counts.get(fighterA.getId()).longValue(), 1L);
    assertEquals(counts.get(fighterB.getId()).longValue(), 4L);
    assertEquals(counts.get(fighterC.getId()).longValue(), 3L);
  }

  @Test
  public void matchFight() {
    assertEquals(koFight, fightRepo.matchFight(
        fighterA.getId(),
        fighterB.getId(),
        eventOne.getDate().toString()).get());

    assertEquals(subFight, fightRepo.matchFight(
        fighterC.getId(),
        fighterB.getId(),
        eventThree.getDate().toString()).get());

    assertEquals(tkoFight, fightRepo.matchFight(
        fighterC.getId(),
        fighterB.getId(),
        eventOne.getDate().toString()).get());

    assertEquals(decFight, fightRepo.matchFight(
        fighterC.getId(),
        fighterB.getId(),
        eventTwo.getDate().toString()).get());
  }

  private Fighter createFighter(String b, Set<Fight> losses, Set<Fight> wins) {
    return fighterRepo.save(new Fighter().setFullname(b)
        .setLosses(losses)
        .setWins(wins));
  }

  private Event createEvent(int i, int i2, String s, ImmutableSet<Fight> of) {
    return eventRepo.save(new Event().setDate(LocalDate.of(2014, i, i2))
        .setName(s)
        .setFights(of));
  }
}
