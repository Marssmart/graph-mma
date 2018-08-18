package org.deer.mma.stats.reactor.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SherdogParser {

  private final Document document;

  public SherdogParser(final String fileContent) {
    document = Jsoup.parse(fileContent);
  }

  public Elements getFightRecordRows() {
    return document.select("div.module.fight_history")
        .select("div.content.table")
        .select("tr:nth-child(n+2)");
  }
}
