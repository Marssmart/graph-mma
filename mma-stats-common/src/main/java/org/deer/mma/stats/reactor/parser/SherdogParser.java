package org.deer.mma.stats.reactor.parser;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;

public class SherdogParser {

  private final List<SherdogFightRecord> fightRecords;
  private final String fighterName;

  public SherdogParser(final Document document) {
    fighterName = document.select("div.module.bio_fighter.vcard")
        .select("h1")
        .select("span.fn")
        .text();

    fightRecords = document.select("div.module.fight_history")
        .select("div.content.table")
        .select("tr:nth-child(n+2):has(span.final_result)")
        .stream()
        .map(row -> new SherdogFightRecord()
            .setEventName(row.select("td")
                .select("a:has(span)")
                .select("span[itemprop='award']")
                .text())
            .setEventLink(row.select("td")
                .select("a:has(span)")
                .attr("href"))
            .setEventDate(row.select("td:has(a)")
                .select("span.sub_line")
                .text())
            .setFightEnd(row.select("td")
                .select("span.final_result")
                .text())
            .setFightEndType(row.select("td:not(:has(a)):has(span):has(br)")
                .first()
                .ownText())
            .setReferee(row.select("td:not(:has(a))")
                .select("span.sub_line")
                .text())
            .setStopageRound(row.select("td:not(:has(a)):not(:has(span))")
                .first()
                .text())
            .setStopageTime(row.select("td:not(:has(a)):not(:has(span))")
                .last()
                .text())
            .setOpponentLink(row.select("td")
                .select("a:not(:has(span))[href*='/fighter/']")
                .attr("href"))
            .setOpponentName(row.select("td")
                .select("a:not(:has(span))[href*='/fighter/']")
                .text()))
        .collect(Collectors.toList());
  }

  public List<SherdogFightRecord> getFightRecords() {
    return fightRecords;
  }

  public String getFighterName() {
    return fighterName;
  }

  public static class SherdogFightRecord {

    private String eventName;
    private String eventLink;
    private String eventDate;
    private String fightEnd;
    private String fightEndType;
    private String referee;
    private String stopageRound;
    private String stopageTime;
    private String opponentName;
    private String opponentLink;

    public Optional<String> getEventName() {
      return Optional.ofNullable(eventName);
    }

    SherdogFightRecord setEventName(String eventName) {
      this.eventName = Strings.emptyToNull(eventName);
      return this;
    }

    public Optional<String> getEventLink() {
      return Optional.ofNullable(eventLink);
    }

    SherdogFightRecord setEventLink(String eventLink) {
      this.eventLink = Strings.emptyToNull(eventLink);
      return this;
    }

    public Optional<String> getEventDate() {
      return Optional.ofNullable(eventDate);
    }

    SherdogFightRecord setEventDate(String eventDate) {
      this.eventDate = Strings.emptyToNull(eventDate);
      return this;
    }

    public Optional<String> getFightEnd() {
      return Optional.ofNullable(fightEnd);
    }

    SherdogFightRecord setFightEnd(String fightEnd) {
      this.fightEnd = Strings.emptyToNull(fightEnd);
      return this;
    }

    public Optional<String> getFightEndType() {
      return Optional.ofNullable(fightEndType);
    }

    SherdogFightRecord setFightEndType(String fightEndType) {
      this.fightEndType = Strings.emptyToNull(fightEndType);
      return this;
    }

    public Optional<String> getReferee() {
      return Optional.ofNullable(referee);
    }

    SherdogFightRecord setReferee(String referee) {
      this.referee = Strings.emptyToNull(referee);
      return this;
    }

    public Optional<String> getStopageRound() {
      return Optional.ofNullable(stopageRound);
    }

    SherdogFightRecord setStopageRound(String stopageRound) {
      this.stopageRound = Strings.emptyToNull(stopageRound);
      return this;
    }

    public Optional<String> getStopageTime() {
      return Optional.ofNullable(stopageTime);
    }

    SherdogFightRecord setStopageTime(String stopageTime) {
      this.stopageTime = Strings.emptyToNull(stopageTime);
      return this;
    }

    public Optional<String> getOpponentName() {
      return Optional.ofNullable(opponentName);
    }

    SherdogFightRecord setOpponentName(String opponentName) {
      this.opponentName = Strings.emptyToNull(opponentName);
      return this;
    }

    public Optional<String> getOpponentLink() {
      return Optional.ofNullable(opponentLink);
    }

    SherdogFightRecord setOpponentLink(String opponentLink) {
      this.opponentLink = Strings.emptyToNull(opponentLink);
      return this;
    }
  }
}
