package org.deer.mma.stats.db.node.enumerated;

import java.util.EnumSet;
import java.util.Optional;

public enum FightEnd {

  WIN,
  LOSS,
  DRAW,
  NC,
  N_A;

  public static Optional<FightEnd> valueForName(final String name) {
    return EnumSet.allOf(FightEnd.class).stream()
        .filter(fightEnd -> fightEnd.name().toLowerCase().equals(name.toLowerCase()))
        .findFirst();
  }
}
