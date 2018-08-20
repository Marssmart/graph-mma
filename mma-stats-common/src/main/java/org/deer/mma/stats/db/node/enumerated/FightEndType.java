package org.deer.mma.stats.db.node.enumerated;

public enum FightEndType {

  DECISION_SPLIT,
  DECISION_UNANIMOUS,
  DECISION_MAJORITY,
  TKO,
  KO,
  SUBMISSION,
  N_A;

  public static FightEndType match(final String input) {
    final String standardInput = input.trim().toLowerCase();

    if (standardInput.contains("decision")) {
      if (standardInput.contains("unanimous")) {
        return DECISION_UNANIMOUS;
      }
      if (standardInput.contains("major")) {
        return DECISION_MAJORITY;
      }
      return DECISION_SPLIT;
    }

    if (standardInput.contains("ko")) {
      if (standardInput.contains("tko")) {
        return TKO;
      }
      return KO;
    }

    if (standardInput.contains("sub")) {
      return SUBMISSION;
    }
    return N_A;
  }
}
