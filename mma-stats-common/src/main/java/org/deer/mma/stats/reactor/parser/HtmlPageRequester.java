package org.deer.mma.stats.reactor.parser;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.lang.NonNull;

public interface HtmlPageRequester {

  CompletableFuture<Optional<String>> requestLink(@NonNull final String link);
}
