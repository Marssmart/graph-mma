package org.deer.mma.stats.db.node.convertor;

import java.time.Duration;
import java.util.Optional;
import org.neo4j.ogm.typeconversion.AttributeConverter;

public class DurationConvertor implements AttributeConverter<Duration, Long> {

  @Override
  public Long toGraphProperty(Duration value) {
    return Optional.ofNullable(value)
        .map(Duration::getSeconds)
        .orElse(null);
  }

  @Override
  public Duration toEntityAttribute(Long value) {
    return Optional.ofNullable(value)
        .map(Duration::ofSeconds)
        .orElse(null);
  }
}
