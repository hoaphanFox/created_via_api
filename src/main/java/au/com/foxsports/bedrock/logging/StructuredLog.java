package au.com.foxsports.bedrock.logging;

import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.exception.ExceptionUtils.getMessage;

/**
 * Log with using Structured Arguments that allows searchable fields in Logstash/Kibana.
 */
public class StructuredLog {

    public static final String ERROR_MESSAGE_KEY = "errorMessage";

    public static final String STACKTRACE_KEY = "stacktrace";

    private static final String ELAPSED_TIME_MS = "elapsedTimeMs";

    private final Logger log;

    private final Map<String, Object> logVarsContext;

    private Optional<StopWatch> stopwatch;

    private StructuredLog(final Logger log, final Optional<StopWatch> stopwatch) {
        this(log, Collections.emptyMap(), stopwatch);
    }

    private StructuredLog(final Logger log,
                          final Map<String, Object> logVarsContext,
                          final Optional<StopWatch> stopwatch) {
        this.log = log;
        this.logVarsContext = logVarsContext;
        this.stopwatch = stopwatch;
    }

    public static StructuredLog withLogger(final Logger log) {
        return new StructuredLog(log, Optional.empty());
    }

    public StructuredLog with(final String key, final Object value) {
        return with(Pair.of(key, value));
    }

    @SafeVarargs
    public final StructuredLog with(final Pair<String, Object>... pairs) {
        final Map<String, Object> logVarsContext = Optional.ofNullable(this.logVarsContext)
                .map(HashMap::new)
                .orElse(new HashMap<>());
        Stream.of(pairs).forEach(pair -> logVarsContext.put(pair.getKey(), pair.getValue()));
        return new StructuredLog(log, logVarsContext, stopwatch);
    }

    public StructuredLog withStopwatch() {
        this.stopwatch = Optional.of(StopWatch.createStarted());
        return this;
    }

    public StructuredLog info(final String message) {
        log(log::info, message);
        return this;
    }

    public StructuredLog debug(final String message) {
        log(log::debug, message);
        return this;
    }

    public StructuredLog warn(final String message) {
        log(log::warn, message);
        return this;
    }

    public StructuredLog error(final String message) {
        log(log::error, message);
        return this;
    }

    public StructuredLog error(final String message, final Throwable t) {
        if (nonNull(t)) {
            List<StructuredArgument> structuredArguments = getStructuredArguments();
            structuredArguments.add(StructuredArguments.keyValue(ERROR_MESSAGE_KEY, getMessage(t)));
            log(log::error, message, message);
            log.error(STACKTRACE_KEY, t);
        }
        return this;
    }

    private List<StructuredArgument> getStructuredArguments() {
        final List<StructuredArgument> structuredArguments = logVarsContext.entrySet()
                .stream()
                .map(this::toStructuredArgument)
                .collect(Collectors.toList());

        stopwatch.map(this::toStructuredArgument).ifPresent(structuredArguments::add);

        return structuredArguments;
    }

    private StructuredArgument toStructuredArgument(final StopWatch stopWatch) {
        return StructuredArguments.keyValue(ELAPSED_TIME_MS, stopWatch.getTime(MILLISECONDS));
    }

    private StructuredArgument toStructuredArgument(final Map.Entry<String, Object> entry) {
        return StructuredArguments.keyValue(entry.getKey(), entry.getValue());
    }

    private void log(final LogFunction function, final String message) {
        log(function, message, getStructuredArguments().toArray());
    }

    private void log(final LogFunction function, final String message, Object... args) {
        function.log(message, args);
    }

    @FunctionalInterface
    private interface LogFunction { void log(String message, Object... args); }
}
