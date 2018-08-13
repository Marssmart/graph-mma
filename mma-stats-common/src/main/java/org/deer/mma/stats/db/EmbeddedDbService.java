package org.deer.mma.stats.db;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmbeddedDbService implements DbService {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedDbService.class);

    @Value("${neo.db.file.path}")
    private String neoDbFilePath;

    private GraphDatabaseService graphDatabaseService;

    @Autowired
    EmbeddedDbService() {
        graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(new File(neoDbFilePath));
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        LOG.info("Shutdown hook added");
    }

    public Transaction newTransaction() {
        return graphDatabaseService.beginTx();
    }

    @Override
    public void close() {
        LOG.info("Closing DB service");
        graphDatabaseService.shutdown();
    }
}
