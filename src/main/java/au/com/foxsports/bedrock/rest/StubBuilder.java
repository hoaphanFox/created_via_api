package au.com.foxsports.bedrock.rest;

import brave.Tracing;
import brave.okhttp3.TracingCallFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import okhttp3.*;
import org.slf4j.Logger;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class StubBuilder<T> {

    private static final Logger log = getLogger(StubBuilder.class);

    private OkHttpClient.Builder clientBuilder;

    private final String url;

    private final Class<T> stubClass;

    private final ObjectMapper objectMapper;

    private MeterRegistry meterRegistry;

    private Tracing tracing;

    public StubBuilder(final Class<T> stubClass, final String url, final ObjectMapper objectMapper) {
        clientBuilder = new OkHttpClient().newBuilder().followRedirects(false);
        this.objectMapper = objectMapper;
        this.stubClass = stubClass;
        this.url = url;
    }

    public StubBuilder<T> withMeterRegistry(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        clientBuilder.eventListener(OkHttpMetricsEventListener
                .builder(meterRegistry, "okhttpclient.call")
                .uriMapper(rq -> rq.url().toString())
                .build());
        return this;
    }

    public StubBuilder<T> withBearerTokenAuthentication(final Supplier<String> tokenSupplier) {
        clientBuilder = clientBuilder.addInterceptor(chain -> {
            Request request = chain.request();
            if (isBlank(request.header(AUTHORIZATION))) {
                request = request.newBuilder().header(AUTHORIZATION, "Bearer " + tokenSupplier.get()).build();
            }
            return chain.proceed(request);
        });
        return this;
    }

    public StubBuilder<T> withBasicAuthentication(final String username, final String password) {
        clientBuilder = clientBuilder.addInterceptor((chain) -> {
            Request request = chain.request();
            if (isBlank(request.header(AUTHORIZATION))) {
                request = request.newBuilder().header(AUTHORIZATION,
                        Credentials.basic(username, password))
                        .build();
            }
            return chain.proceed(request);
        });
        return this;
    }

    public StubBuilder<T> withThrowExceptionOnError() {
        clientBuilder = clientBuilder.addInterceptor((chain) -> {
            final Response response = chain.proceed(chain.request());
            if (!response.isSuccessful()) {
                log.error("Response from server is not successful ({}). Body: {}", response.code(), response.body());
                throw new StubException(response.code(), response.message());
            }
            return response;
        });
        return this;
    }

    public StubBuilder<T> withInterceptor(final Interceptor interceptor) {
        clientBuilder = clientBuilder.addInterceptor(interceptor);
        return this;
    }

    public StubBuilder<T> withLogging(final Logger logger, final boolean logHeaders, final boolean logBody) {
        clientBuilder = clientBuilder.addInterceptor(new LoggingInterceptor(logger, logHeaders, logBody));
        return this;
    }

    public StubBuilder<T> withLogging(final Logger logger, final LogConfig logConfig) {
        clientBuilder = clientBuilder.addInterceptor(new LoggingInterceptor(logger, logConfig));
        return this;
    }

    public StubBuilder<T> withUserAgent(final String userAgent) {
        return withHeader("User-Agent", userAgent);
    }

    public StubBuilder<T> withHeader(final String headerName, final String headerValue) {
        clientBuilder = clientBuilder.addInterceptor((chain) -> {
            Request request = chain.request();
            if (isBlank(request.header(headerName))) {
                request = request.newBuilder().header(headerName, headerValue).build();
            }
            return chain.proceed(request);
        });
        return this;
    }

    public StubBuilder<T> withTimeout(final long timeout, final TimeUnit timeUnit) {
        clientBuilder = clientBuilder.readTimeout(timeout, timeUnit).writeTimeout(timeout, timeUnit);
        return this;
    }

    public StubBuilder<T> withRetryOnConnectionFailure(final boolean retryOnConnectionFailure) {
        clientBuilder = clientBuilder.retryOnConnectionFailure(retryOnConnectionFailure);
        return this;
    }

    public StubBuilder<T> withTracing(final Tracing tracing) {
        this.tracing = tracing;
        return this;
    }

    public StubBuilder<T> withConnectionPool(ConnectionPool connectionPool) {
        clientBuilder.connectionPool(connectionPool);
        return this;
    }

    public StubBuilder<T> withDispatcher(Dispatcher dispatcher) {
        clientBuilder.dispatcher(dispatcher);
        return this;
    }

    public T build() {
        final OkHttpClient okHttpClient = clientBuilder.build();

        gauge("okhttpclient.dispatcher.queued", okHttpClient.dispatcher(), Dispatcher::queuedCallsCount);
        gauge("okhttpclient.dispatcher.running", okHttpClient.dispatcher(), Dispatcher::runningCallsCount);
        gauge("okhttpclient.connections.total", okHttpClient.connectionPool(), ConnectionPool::connectionCount);
        gauge("okhttpclient.connections.idle", okHttpClient.connectionPool(), ConnectionPool::idleConnectionCount);

        final Retrofit.Builder builder = new Retrofit.Builder();
        if (tracing != null) {
            builder.callFactory(TracingCallFactory.create(tracing, okHttpClient));
        } else {
            builder.client(okHttpClient);
        }
        return builder
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build()
                .create(stubClass);
    }

    private <T> void gauge(final String name, final T metricTarget, final ToDoubleFunction<T> metricSupplier) {
        if (meterRegistry != null) {
            Gauge.builder(name, metricTarget, metricSupplier)
                    .tag("url", url)
                    .register(meterRegistry);
        }
    }
}
