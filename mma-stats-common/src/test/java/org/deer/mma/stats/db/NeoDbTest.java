package org.deer.mma.stats.db;

import org.deer.mma.stats.TestConfig;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public abstract class NeoDbTest {

  @Autowired
  protected EmbeddedDbService dbService;

  @After
  public void cleanup() {
    dbService.getDbService().execute("MATCH (n) DETACH DELETE n");
  }
}
