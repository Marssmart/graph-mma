package org.deer.mma.stats.db.repository;

import org.deer.mma.stats.TestConfig;
import org.junit.After;
import org.junit.runner.RunWith;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public abstract class RepositoryTest {

  @Autowired
  private SessionFactory sessionFactory;

  @After
  public void cleanup() {
    Session session = sessionFactory.openSession();
    session.purgeDatabase();
  }
}
