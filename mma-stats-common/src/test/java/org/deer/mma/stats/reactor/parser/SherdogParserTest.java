package org.deer.mma.stats.reactor.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.deer.mma.stats.db.node.enumerated.FightEnd;
import org.deer.mma.stats.db.node.enumerated.FightEndType;
import org.deer.mma.stats.reactor.parser.SherdogParser.SherdogFightRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class SherdogParserTest {

  @Test
  public void parseAndVerifyMappable() throws IOException {
    final Document document = parseDocument("sherdog/sherdog-robert-whittaker.html");
    final SherdogParser parser = new SherdogParser(document);

    final List<SherdogFightRecord> fightRecords = parser.getFightRecords();
    assertFalse(fightRecords.isEmpty());
    assertEquals(24, fightRecords.size());
    assertEquals("Robert Whittaker", parser.getFighterName());

    fightRecords.forEach(record -> {
      final Optional<FightEnd> fightEnd = FightEnd.valueForName(record.getFightEnd().orElse(null));
      final Optional<FightEndType> fightEndType = FightEndType
          .valueForName(record.getFightEndType().orElse(null));

      assertTrue("Cannot map " + record.getFightEnd() + " to " + Arrays.toString(FightEnd.values()),
          fightEnd.isPresent());
      assertTrue("Cannot map " + record.getFightEndType() + " to " + Arrays
          .toString(FightEndType.values()), fightEndType.isPresent());
    });
  }

  private static Document parseDocument(String path) throws IOException {
    return Jsoup.parse(Resources.toString(Resources.getResource(path), StandardCharsets.UTF_8));
  }
}