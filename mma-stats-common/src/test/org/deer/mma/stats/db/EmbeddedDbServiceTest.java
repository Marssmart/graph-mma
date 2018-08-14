package org.deer.mma.stats.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;

public class EmbeddedDbServiceTest {

    private static final String DB_PATH = "test/data";
    private EmbeddedDbService dbService;

    @Before
    public void init(){
        dbService = new EmbeddedDbService(DB_PATH);
    }

    @Test
    public void testNewTx(){
        dbService.newTransaction().close();
    }

    @After
    public void cleanup(){
        dbService.close();
        FileSystemUtils.deleteRecursively(new File(DB_PATH));
    }
}