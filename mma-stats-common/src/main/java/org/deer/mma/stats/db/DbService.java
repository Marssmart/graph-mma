package org.deer.mma.stats.db;

import org.neo4j.graphdb.Transaction;

public interface DbService extends AutoCloseable {

  Transaction newTransaction();
}
