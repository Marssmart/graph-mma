package org.deer.mma.stats.reactor.service;

import static org.deer.mma.stats.db.node.enumerated.FightEnd.N_A;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.math.NumberUtils;
import org.deer.mma.stats.db.node.Event;
import org.deer.mma.stats.db.node.Fight;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.db.node.Referee;
import org.deer.mma.stats.db.node.enumerated.FightEnd;
import org.deer.mma.stats.db.node.enumerated.FightEndType;
import org.deer.mma.stats.db.repository.EventRepo;
import org.deer.mma.stats.db.repository.FightRepo;
import org.deer.mma.stats.db.repository.FighterRepo;
import org.deer.mma.stats.db.repository.RefereeRepo;
import org.deer.mma.stats.reactor.parser.SherdogParser;
import org.deer.mma.stats.reactor.parser.SherdogParser.SherdogFightRecord;
import org.deer.mma.stats.reactor.request.HtmlPageRequester;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SherdogDiscoveryService {

  private static final Logger LOG = LoggerFactory.getLogger(SherdogDiscoveryService.class);
  private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter
      .ofPattern("LLL/dd/yyyy", Locale.ENGLISH);
  private static final String SHERDOG_COM = "http://sherdog.com";

  @Autowired
  @Qualifier("rendering")
  private HtmlPageRequester htmlPageRequester;

  @Autowired
  private FighterRepo fighterRepo;

  @Autowired
  private EventRepo eventRepo;

  @Autowired
  private RefereeRepo refereeRepo;

  @Autowired
  private FightRepo fightRepo;

  public void triggerSherdogDiscovery() {
    final Map<String, Event> eventsIndex = eventRepo.findAllAsStream()
        .collect(Collectors.toMap(Event::getName, o -> o));

    final Map<String, Referee> refereeIndex = refereeRepo.findAllAsStream()
        .collect(Collectors.toMap(Referee::getName, r -> r));

    final Map<String, Fighter> fightersIndex = fighterRepo.findAllAsStream()
        .collect(Collectors.toMap(Fighter::getFullname, f -> f));

    final Long total = fighterRepo.countBySherdogLinkIsNotNull();

    IntStream.range(0, (int) (total + (total % 10 == 0 ? 0 : 1)))
        .mapToObj(i -> PageRequest.of(i, 10))
        .forEach(pageRequest -> {

          LOG.info("Requesting page {}", pageRequest);
          requestWebPagesForDbPage(pageRequest)
              .forEach(document -> {
                LOG.info("Processing document {}", document.title());
                final SherdogParser parser = new SherdogParser(document);
                LOG.info("Document {} parsed", document.title());
                final String fighterName = parser.getFighterName();
                if (!fightersIndex.containsKey(fighterName)) {
                  LOG.error("Fighter name not present in document {}!!!", document.title());
                  return;
                }
                LOG.info("Processing document {}", document.title());
                parser.getFightRecords()
                    .forEach(record -> {
                      final Optional<Referee> fightReferee = createOrMergeReferee(
                          refereeIndex, record);

                      final Optional<Fight> fight = createOrMergeFight(fightersIndex,
                          parser, record, fightReferee);

                      final Optional<Event> event = createOrMergeEvent(eventsIndex,
                          record, fight);

                      final Optional<FightEnd> fightEnd = record.getFightEnd()
                          .map(val -> FightEnd.valueForName(val).orElse(null));

                      final Optional<String> opponentName = record.getOpponentName();

                      if (opponentName.isPresent()) {
                        final Fighter opponent = fightersIndex
                            .getOrDefault(opponentName.get(), new Fighter())
                            .setFullname(opponentName.get());

                        final Fighter fighter = fightersIndex.get(fighterName);

                        record.getOpponentLink().map(link -> SHERDOG_COM + link)
                            .ifPresent(opponent::setSherdogLink);

                        fight.ifPresent(currentFight ->
                            fightEnd.ifPresent(mapNewFightForBothFighters(fighterName, opponent,
                                fighter, currentFight)));

                        fightersIndex.merge(fighterName, fighterRepo.save(fighter),
                            (oldFighter, newFighter) -> newFighter);

                        fightersIndex.merge(opponent.getFullname(), fighterRepo.save(opponent),
                            (oldFighter, newFighter) -> newFighter);
                      }
                    });
              });
          LOG.info("Page {} processed", pageRequest);
        });
  }

  private static Consumer<FightEnd> mapNewFightForBothFighters(String fighterName, Fighter opponent,
      Fighter fighter, Fight currentFight) {
    return currentFightEnd -> {
      switch (currentFightEnd) {
        case WIN: {
          addWin(fighter, currentFight);
          addLoss(opponent, currentFight);
          break;
        }
        case LOSS: {
          addLoss(fighter, currentFight);
          addWin(opponent, currentFight);
          break;
        }
        case NC: {
          addNoContest(fighter, currentFight);
          addNoContest(opponent, currentFight);
          break;
        }
        case DRAW: {
          addDraw(fighter, currentFight);
          addDraw(opponent, currentFight);
          break;
        }
        case N_A: {
          LOG.warn("{} fight end detected for {}, fighter {}",
              N_A, fighterName);
          break;
        }
      }
    };
  }

  private static void addDraw(Fighter fighter, Fight currentFight) {
    fighter.setDraws(ImmutableSet.<Fight>builder()
        .addAll(Optional.ofNullable(fighter.getDraws())
            .orElse(Collections.emptySet()))
        .add(currentFight)
        .build());
  }

  private static void addNoContest(Fighter fighter, Fight currentFight) {
    fighter.setNc(ImmutableSet.<Fight>builder()
        .addAll(Optional.ofNullable(fighter.getNc())
            .orElse(Collections.emptySet()))
        .add(currentFight)
        .build());
  }

  private static void addLoss(Fighter fighter, Fight currentFight) {
    fighter.setLosses(ImmutableSet.<Fight>builder()
        .addAll(Optional.ofNullable(fighter.getLosses())
            .orElse(Collections.emptySet()))
        .add(currentFight)
        .build());
  }

  private static void addWin(Fighter fighter, Fight currentFight) {
    fighter.setWins(ImmutableSet.<Fight>builder()
        .addAll(Optional.ofNullable(fighter.getWins())
            .orElse(Collections.emptySet()))
        .add(currentFight)
        .build());
  }

  private List<Document> requestWebPagesForDbPage(PageRequest pageRequest) {

    final AtomicInteger success = new AtomicInteger();
    final AtomicInteger error = new AtomicInteger();

    final List<CompletableFuture<Document>> documentFutures = fighterRepo
        .findBySherdogLinkIsNotNull(pageRequest)
        .stream()
        .map(fighter -> {
          final String sherdogLink = fighter.getSherdogLink();
          final String fullname = fighter.getFullname();
          LOG.info("Requesting sherdog for fighter {},link {}", fullname, sherdogLink);
          return htmlPageRequester.requestLink(sherdogLink)
              .thenApply(content -> {
                LOG.info("Sherdog content for fighter {} received", fullname);
                return Jsoup.parse(content.orElse("N/A"));
              }).whenComplete((aVoid, throwable) -> {
                if (throwable != null) {
                  error.incrementAndGet();
                  LOG.trace("Error while requesting {}", sherdogLink, throwable);
                } else {
                  success.incrementAndGet();
                  LOG.info("Data discovery for fighter {} finished", fullname);
                }
              });
        }).collect(Collectors.toList());

    try {
      CompletableFuture.allOf(documentFutures.stream().toArray(CompletableFuture[]::new))
          .get(5, TimeUnit.MINUTES);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      LOG.trace("Web page requesting for page {} partially unsuccessful", e);
    }
    LOG.info("Web page requesting for page {} finished[ok {},error {}]", pageRequest, success.get(),
        error.get());

    return documentFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
  }

  private Optional<Event> createOrMergeEvent(Map<String, Event> eventsIndex,
      SherdogFightRecord record,
      Optional<Fight> fight) {
    final Optional<String> eventName = record.getEventName();
    if (eventName.isPresent()) {
      final Event event = eventsIndex
          .getOrDefault(eventName.get(), new Event())
          .setName(eventName.get());

      record.getEventLink()
          .map(link -> SHERDOG_COM + link)
          .ifPresent(event::setSherdogLink);

      record.getEventDate()
          .map(date -> date.replace(" ", ""))
          .map(date -> {
            try {
              return EVENT_DATE_FORMATTER.parse(date);
            } catch (DateTimeParseException e) {
              LOG.warn("Error parsing {} as date", date);
              return null;
            }
          })
          .map(LocalDate::from)
          .ifPresent(event::setDate);

      fight.map(currentFight -> ImmutableSet.<Fight>builder()
          .addAll(Optional.ofNullable(event.getFights()).orElse(Collections.emptySet()))
          .add(currentFight)
          .build()).ifPresent(event::setFights);

      return Optional.of(eventsIndex
          .merge(eventName.get(), eventRepo.save(event), (oldEvent, newEvent) -> newEvent));
    }
    return Optional.empty();
  }

  private Optional<Fight> createOrMergeFight(Map<String, Fighter> fightersIndex,
      SherdogParser parser,
      SherdogFightRecord record, Optional<Referee> fightReferee) {
    final Optional<String> fightEnd = record.getFightEnd();
    if (fightEnd.isPresent()) {
      final Optional<Byte> stopageRound = record.getStopageRound()
          .filter(NumberUtils::isCreatable)
          .map(Byte::parseByte);

      final Optional<Duration> stopageTime = record.getStopageTime()
          .filter(time -> !time.contains("N/A"))
          .map(time -> time.split(":"))
          .filter(timeParts -> timeParts.length == 2)
          .filter(timeParts -> Arrays.stream(timeParts).map(String::trim)
              .allMatch(NumberUtils::isDigits))
          .map(timeParts -> Duration
              .ofMinutes(Integer.parseInt(timeParts[0]))
              .plusSeconds(Integer.parseInt(timeParts[1])));

      final Optional<FightEndType> fightEndType = FightEndType
          .valueForName(record.getFightEndType().orElse(FightEndType.N_A.name()));
      final Fight fight = fightRepo.matchByFighterAndStopageTimeAndRound(
          fightersIndex.get(parser.getFighterName()).getId(),
          fightEndType.orElse(null),
          stopageRound.orElse(null),
          stopageTime.map(Fight::convertDuration).orElse(null))
          .orElse(new Fight());

      record.getFightEndType().ifPresent(FightEndType::valueForName);
      fightReferee.ifPresent(fight::setReferee);
      stopageRound.ifPresent(fight::setNumberOfRounds);
      stopageTime.ifPresent(fight::setStoppageTime);
      fightEndType.ifPresent(fight::setFightEndType);

      return Optional.of(fightRepo.save(fight));
    }
    return Optional.empty();
  }

  private Optional<Referee> createOrMergeReferee(Map<String, Referee> refereeIndex,
      SherdogFightRecord record) {
    final Optional<String> refereeName = record.getReferee();
    if (refereeName.isPresent()) {
      final Referee referee = refereeIndex
          .getOrDefault(refereeName.get(), new Referee())
          .setName(refereeName.get());

      return Optional.of(refereeIndex
          .merge(refereeName.get(), refereeRepo.save(referee), (oldRef, newRef) -> newRef));
    }
    return Optional.empty();
  }
}
