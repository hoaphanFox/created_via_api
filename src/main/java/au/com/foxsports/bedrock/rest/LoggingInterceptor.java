package au.com.foxsports.bedrock.rest;

import au.com.foxsports.bedrock.logging.StructuredLog;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class LoggingInterceptor implements Interceptor {

    private static final String NO_LOG = "<omitted>";

    private final Logger logger;

    private final LogConfig logConfig;

    public LoggingInterceptor(final Logger logger, final LogConfig logConfig) {
        this.logger = logger;
        this.logConfig = logConfig;
    }

    public LoggingInterceptor(final Logger logger, final boolean logHeaders, final boolean logBody) {
        this.logger = logger;
        this.logConfig = LogConfig.builder()
                .logRequestHeaders(logHeaders)
                .logResponseHeaders(logHeaders)
                .logRequestBody(logBody)
                .logResponseBody(logBody)
                .build();
    }

    @Override
    public Response intercept(final Interceptor.Chain chain) throws IOException {
        final String sequence = UUID.randomUUID().toString();

        final Optional<StructuredLog> slog = ofNullable(logger)
                .map(StructuredLog::withLogger)
                .map(structLog -> structLog.with("sequence", sequence));

        slog.ifPresent(setRequestDetails(chain));

        final StopWatch stopWatch = StopWatch.createStarted();

        try {
            final Response response = chain.proceed(chain.request());
            slog.ifPresent(setResponseDetails(stopWatch, response));
            return response;
        } catch (RuntimeException | IOException e) {
            slog.ifPresent(setErrorDetails(stopWatch, e));
            throw e;
        }
    }

    private Consumer<StructuredLog> setRequestDetails(final Interceptor.Chain chain) {
        final Request request = chain.request();
        final String requestHeaders = logConfig.isLogRequestHeaders() ? request.headers().toString() : NO_LOG;
        final String requestBody = logConfig.isLogRequestBody() ? getRequestBody(chain) : NO_LOG;

        return structLog -> {
            final HttpUrl url = request.url();
            final String method = request.method();

            structLog.with(
                    Pair.of("url", url.url()),
                    Pair.of("method", method),
                    Pair.of("remoteHost", url.host()),
                    Pair.of("headers", logConfig.isLogRequestHeaders() ? requestHeaders : NO_LOG),
                    Pair.of("requestBody", requestBody)
            ).debug(format("Request {0} {1}", method, url));
        };
    }

    private Consumer<StructuredLog> setResponseDetails(final StopWatch stopWatch, final Response response) {
        final String responseHeaders = logConfig.isLogResponseHeaders() ? response.headers().toString() : NO_LOG;
        final String responseBody = logConfig.isLogResponseBody() ? getResponseBody(response) : NO_LOG;

        return structLog -> {
            final int responseCode = response.code();
            final String responseMessage = response.message();

            structLog.with(
                    Pair.of("responseCode", responseCode),
                    Pair.of("responseMessage", responseMessage),
                    Pair.of("elapsedTimeMs", stopWatch.getTime(MILLISECONDS)),
                    Pair.of("headers", responseHeaders),
                    Pair.of("responseBody", responseBody)
            ).debug(format("Response {0} {1}", responseCode, responseMessage));
        };
    }

    private Consumer<StructuredLog> setErrorDetails(final StopWatch stopWatch, final Exception e) {
        return structLog -> structLog.with(Pair.of("elapsedTimeMs", stopWatch.getTime(MILLISECONDS)))
                .error("Unexpected error in http call", e);
    }

    private String getResponseBody(final Response response) {
        return Optional.ofNullable(response)
                .filter(res -> res.body() != null)
                .map(res -> response.body().source())
                .map(buffer -> {
                    try {
                        buffer.request(Long.MAX_VALUE);
                        return buffer;
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .map(buffer -> buffer.getBuffer().clone().readString(UTF_8))
                .filter(StringUtils::isNotBlank)
                .orElse("N/A");
    }

    private String getRequestBody(final Interceptor.Chain chain) {
        return Optional.of(chain.request())
                .filter(req -> req.body() != null)
                .map(req -> {
                    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try (Buffer buffer = new Buffer()) {
                        chain.request().body().writeTo(buffer);
                        bos.write(buffer.readByteArray());
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                    return new String(bos.toByteArray(), UTF_8);
                })
                .filter(StringUtils::isNotBlank)
                .orElse("N/A");
    }
}
