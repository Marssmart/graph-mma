package org.deer.mma.stats.reactor.parser;

import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.apache.http.client.HttpClient;
import org.deer.mma.stats.TestConfig;
import org.deer.mma.stats.reactor.request.BasicHttpRequester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class BasicHttpRequesterTest {

  @Autowired
  private HttpClient httpClient;

  private BasicHttpRequester requester;

  @Before
  public void setUp() {
    requester = new BasicHttpRequester(httpClient);
  }

  @Test
  public void requestLink() {
    Optional<String> content = requester
        .requestLink("http://www.fightmatrix.com/fighter-profile/Gegard+Mousasi/13436/").join();
    assertTrue(content.isPresent());
    assertTrue(content.get().startsWith("<!DOCTYPE html>"));
  }
}