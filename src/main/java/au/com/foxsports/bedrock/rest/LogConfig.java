package au.com.foxsports.bedrock.rest;

import java.beans.ConstructorProperties;

/**
 * Used by {@link LoggingInterceptor} to determine whether to log request/response body and request/response headers.
 */
public class LogConfig {

    private boolean logRequestHeaders;

    private boolean logRequestBody;

    private boolean logResponseHeaders;

    private boolean logResponseBody;

    @ConstructorProperties({"logRequestHeaders", "logRequestBody", "logResponseHeaders", "logResponseBody"})
    LogConfig(boolean logRequestHeaders, boolean logRequestBody, boolean logResponseHeaders, boolean logResponseBody) {
        this.logRequestHeaders = logRequestHeaders;
        this.logRequestBody = logRequestBody;
        this.logResponseHeaders = logResponseHeaders;
        this.logResponseBody = logResponseBody;
    }

    public static LogConfig.LogConfigBuilder builder() {
        return new LogConfig.LogConfigBuilder();
    }

    public boolean isLogRequestHeaders() {
        return this.logRequestHeaders;
    }

    public boolean isLogRequestBody() {
        return this.logRequestBody;
    }

    public boolean isLogResponseHeaders() {
        return this.logResponseHeaders;
    }

    public boolean isLogResponseBody() {
        return this.logResponseBody;
    }

    public static class LogConfigBuilder {

        private boolean logRequestHeaders;

        private boolean logRequestBody;

        private boolean logResponseHeaders;

        private boolean logResponseBody;

        public LogConfig.LogConfigBuilder logRequestHeaders(boolean logRequestHeaders) {
            this.logRequestHeaders = logRequestHeaders;
            return this;
        }

        public LogConfig.LogConfigBuilder logRequestBody(boolean logRequestBody) {
            this.logRequestBody = logRequestBody;
            return this;
        }

        public LogConfig.LogConfigBuilder logResponseHeaders(boolean logResponseHeaders) {
            this.logResponseHeaders = logResponseHeaders;
            return this;
        }

        public LogConfig.LogConfigBuilder logResponseBody(boolean logResponseBody) {
            this.logResponseBody = logResponseBody;
            return this;
        }

        public LogConfig build() {
            return new LogConfig(logRequestHeaders, logRequestBody, logResponseHeaders, logResponseBody);
        }
    }
}
