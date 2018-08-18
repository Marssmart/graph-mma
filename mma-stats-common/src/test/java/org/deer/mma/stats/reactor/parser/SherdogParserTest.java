package org.deer.mma.stats.reactor.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

public class SherdogParserTest {

  private SherdogParser parser;

  @Before
  public void init() throws IOException {
    parser = new SherdogParser(Resources.toString(
        Resources.getResource("sherdog-example-payload,html"),
        StandardCharsets.UTF_8));
  }

  @Test
  public void parseFightRecord() {
    Iterator<Element> iterator = parser.getFightRecordRows().iterator();
    assertTrue(iterator.hasNext());

    final Element yoelRomero = iterator.next();
    assertEquals("win", yoelRomero.select("td")
        .select("span.final_result")
        .text());

    assertEquals("/fighter/Yoel-Romero-60762", yoelRomero.select("td")
        .select("a:not(span)")
        .attr("href"));

    assertEquals("/events/UFC-225-Whittaker-vs-Romero-2-65421", yoelRomero.select("td")
        .select("a:has(span)")
        .attr("href"));

    assertEquals("Jun / 09 / 2018", yoelRomero.select("td:has(a)")
        .select("span.sub_line")
        .text());

    assertEquals("Decision (Split)", yoelRomero.select("td:not(:has(a)):has(span):has(br)")
        .first()
        .ownText());

    assertEquals("Dan Miragliotta", yoelRomero.select("td:not(:has(a))")
        .select("span.sub_line")
        .text());

    assertEquals("5", yoelRomero.select("td:not(:has(a)):not(:has(span))")
        .first()
        .text());

    assertEquals("5:00", yoelRomero.select("td:not(:has(a)):not(:has(span))")
        .last()
        .text());
  }
}