package org.deer.mma.stats.reactor.service;

import static org.deer.mma.stats.db.node.enumerated.FightEnd.N_A;

import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
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
      .ofPattern("MMM/dd/yyyy");
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
          final AtomicLong successCounter = new AtomicLong();
          final AtomicLong failedCounter = new AtomicLong();

          LOG.info("Requesting page {}", pageRequest);
          final CompletableFuture[] webPageFutures = fighterRepo
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
                    })
                    .whenComplete((aVoid, throwable) -> {
                      if (throwable != null) {
                        failedCounter.incrementAndGet();
                        LOG.trace("Error while requesting {}", sherdogLink, throwable);
                      } else {
                        successCounter.incrementAndGet();
                        LOG.info("Data discovery for fighter {} finished", fullname);
                      }
                    });
              }).toArray(CompletableFuture[]::new);

          CompletableFuture.allOf(webPageFutures)
              .handleAsync((aVoid, err) -> {
                //fuck the exception here

                LOG.info("All pages requested[success {}, failed {}], processing ...",
                    successCounter.get(), failedCounter.get());
                Arrays.stream(webPageFutures)
                    .filter(fut -> !fut.isCompletedExceptionally())
                    .map(CompletableFuture::join)
                    .map(content -> (Document) content)
                    .forEach(document -> {
                      LOG.info("Processing document {}", document.title());
                      final SherdogParser parser = new SherdogParser(document);
                      LOG.info("Document {} parsed", document.title());
                      if (!fightersIndex.containsKey(parser.getFighterName())) {
                        LOG.error("Fighter name not present in document {}!!!", document.title());
                        return;
                      }
                      LOG.info("Processing document {}", document.title());
                      parser.getFightRecords()
                          .forEach(record -> {
                            LOG.info("Processing record {} for fighter {}", record,
                                parser.getFighterName());
                            CompletableFuture
                                .runAsync(() -> {
                                  final Optional<Referee> fightReferee = createOrMergeReferee(
                                      refereeIndex, record);

                                  final Optional<Fight> fight = createOrMergeFight(fightersIndex,
                                      parser, record, fightReferee);

                                  final Optional<Event> event = createOrMergeEvent(eventsIndex,
                                      record, fight);

                                  final Optional<FightEnd> fightEnd = record.getFightEnd()
                                      .map(val ->FightEnd.valueForName(val).orElse(null));

                                  final Optional<String> opponentName = record.getOpponentName();
                                  if (opponentName.isPresent()) {
                                    final Fighter opponent = fightersIndex
                                        .getOrDefault(opponentName.get(), new Fighter())
                                        .setFullname(opponentName.get());

                                    final Fighter fighter = fightersIndex
                                        .get(parser.getFighterName());

                                    record.getOpponentLink().map(link -> SHERDOG_COM + link)
                                        .ifPresent(opponent::setSherdogLink);

                                    fight.ifPresent(currentFight ->
                                        fightEnd.ifPresent(currentFightEnd -> {
                                          switch (currentFightEnd) {
                                            case WIN: {
                                              fighter.setWins(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(fighter.getWins())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              opponent.setLosses(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(opponent.getLosses())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              break;
                                            }
                                            case LOSS: {
                                              fighter.setLosses(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(fighter.getLosses())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              opponent.setWins(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(opponent.getWins())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              break;
                                            }
                                            case NC: {
                                              fighter.setNc(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(fighter.getNc())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              opponent.setNc(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(opponent.getNc())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              break;
                                            }
                                            case DRAW: {
                                              fighter.setDraws(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(fighter.getDraws())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              opponent.setDraws(ImmutableSet.<Fight>builder()
                                                  .addAll(Optional.ofNullable(opponent.getDraws())
                                                      .orElse(Collections.emptySet()))
                                                  .add(currentFight)
                                                  .build());
                                              break;
                                            }
                                            case N_A: {
                                              LOG.warn("{} {} detected for {}, fighter {}", N_A,
                                                  FightEnd.class, record, parser.getFighterName());
                                              break;
                                            }
                                          }
                                        }));

                                    fightersIndex
                                        .merge(parser.getFighterName(), fighterRepo.save(fighter),
                                            (oldFighter, newFighter) -> newFighter);

                                    fightersIndex
                                        .merge(opponent.getFullname(), fighterRepo.save(opponent),
                                            (oldFighter, newFighter) -> newFighter);
                                  }
                                }).whenComplete((result, throwable) -> {
                              if (throwable != null) {
                                LOG.error("Error processing record {} for fighter {}", record,
                                    parser.getFighterName(), throwable);
                              } else {
                                LOG.info("Processing of record {} for fighter {} done", record,
                                    parser.getFighterName());
                              }
                            }).join();

                          });
                    });
                return null;
              }).join();

          LOG.info("Page {} processed", pageRequest);
        });
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
              return LocalDate.parse(date, EVENT_DATE_FORMATTER);
            } catch (DateTimeParseException e) {
              LOG.warn("Error parsing {} as date", date);
              return null;
            }
          }).ifPresent(event::setDate);

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
          .filter(timeParts -> timeParts.length > 0)
          .map(timeParts -> Duration
              .ofMinutes(Integer.parseInt(timeParts[0]))
              .plusSeconds(Integer.parseInt(timeParts[1])));

      final Fight fight = fightRepo.matchByFighterAndStopageTimeAndRound(
          fightersIndex.get(parser.getFighterName()).getId(),
          FightEndType.valueForName(record.getFightEndType().orElse(FightEndType.N_A.name())).orElse(null),
          stopageRound.orElse((byte) -1),
          Fight.convertDuration(stopageTime.orElse(Duration.ZERO)))
          .orElse(new Fight());

      record.getFightEndType().ifPresent(FightEndType::valueForName);
      fightReferee.ifPresent(fight::setReferee);
      stopageRound.ifPresent(fight::setNumberOfRounds);
      stopageTime.ifPresent(fight::setStoppageTime);

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
