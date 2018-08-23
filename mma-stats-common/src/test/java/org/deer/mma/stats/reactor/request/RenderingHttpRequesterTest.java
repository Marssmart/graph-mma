package org.deer.mma.stats.reactor.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.deer.mma.stats.TestConfig;
import org.deer.mma.stats.reactor.parser.SherdogParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class RenderingHttpRequesterTest {

  @Autowired
  private RenderingHttpRequester requester;

  @Repeat(4)
  @Test
  public void requestLink() {
    final Optional<String> pageContent = requester
        .requestLink("http://www.sherdog.com/fighter/Dominick-Reyes-145941")
        .join();
    assertTrue(pageContent.isPresent());
    final Document document = Jsoup.parse(pageContent.get());
    final SherdogParser parser = new SherdogParser(document);
    assertEquals(parser.getFighterName(),"Dominick Reyes");
    assertFalse(parser.getFightRecords().isEmpty());
  }
}