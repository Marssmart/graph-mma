package org.deer.mma.stats.reactor.request;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public interface HtmlPageRequester {

  CompletableFuture<Optional<String>> requestLink(@Nonnull final String link);
}
