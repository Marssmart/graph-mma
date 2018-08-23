package org.deer.mma.stats.db.node.enumerated;

import java.util.Optional;

public enum FightEndType {

  DECISION_SPLIT,
  DECISION_UNANIMOUS,
  DECISION_MAJORITY,
  TKO,
  KO,
  SUBMISSION,
  N_A;

  public static Optional<FightEndType> valueForName(final String name) {
    final String standardInput = name.trim().toLowerCase();

    if (standardInput.contains("decision")) {
      if (standardInput.contains("unanimous")) {
        return Optional.of(DECISION_UNANIMOUS);
      }
      if (standardInput.contains("major")) {
        return Optional.of(DECISION_MAJORITY);
      }
      return Optional.of(DECISION_SPLIT);
    }

    if (standardInput.contains("ko")) {
      if (standardInput.contains("tko")) {
        return Optional.of(TKO);
      }
      return Optional.of(KO);
    }

    if (standardInput.contains("sub")) {
      return Optional.of(SUBMISSION);
    }

    if (standardInput.contains("n/a")) {
      return Optional.of(N_A);
    }
    return Optional.empty();
  }
}
