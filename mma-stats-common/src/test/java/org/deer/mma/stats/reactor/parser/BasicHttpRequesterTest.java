package org.deer.mma.stats.reactor.parser;

import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;

public class BasicHttpRequesterTest {

  private BasicHttpRequester requester;

  @Before
  public void setUp() {
    requester = new BasicHttpRequester(new DefaultHttpClient());
  }

  @Test
  public void requestLink() {
    Optional<String> content = requester
        .requestLink("http://www.fightmatrix.com/fighter-profile/Gegard+Mousasi/13436/").join();
    assertTrue(content.isPresent());
    assertTrue(content.get().startsWith("<!DOCTYPE html>"));
  }
}