package org.deer.mma.stats.db;

import java.util.Optional;
import java.util.function.Supplier;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public interface NeoTransactional {

  default void doInTx(final NoReturnFunction workToDo) {
    try (Transaction transaction = getDbService().beginTx()) {
      try {
        workToDo.doSomething();
        transaction.success();
      } catch (Exception e) {
        transaction.failure();
        throw new IllegalStateException(e);
      }
    }
  }

  default <T> Optional<T> doInTxAndReturnOptional(final Supplier<T> getSomething) {
    try (Transaction transaction = getDbService().beginTx()) {
      try {
        T value = getSomething.get();
        transaction.success();
        return Optional.ofNullable(value);
      } catch (Exception e) {
        transaction.failure();
        throw new IllegalStateException(e);
      }
    }
  }

  GraphDatabaseService getDbService();

  @FunctionalInterface
  interface NoReturnFunction {

    void doSomething() throws Exception;
  }
}
