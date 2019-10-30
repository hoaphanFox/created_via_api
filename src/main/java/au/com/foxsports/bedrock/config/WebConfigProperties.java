package au.com.foxsports.bedrock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;

@ConfigurationProperties(prefix = "web")
public class WebConfigProperties {

    private Cors cors;

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public static class Cors {

        private String[] allowedOrigins = { };

        private String[] allowedMethods = { };

        private long maxAge = 3600;

        public String[] getAllowedOrigins() {
            return Arrays.copyOf(allowedOrigins, allowedOrigins.length);
        }

        public void setAllowedOrigins(String[] allowedOrigins) {
            this.allowedOrigins = Arrays.copyOf(allowedOrigins, allowedOrigins.length);
        }

        public String[] getAllowedMethods() {
            return Arrays.copyOf(allowedMethods, allowedMethods.length);
        }

        public void setAllowedMethods(String[] allowedMethods) {
            this.allowedMethods = Arrays.copyOf(allowedMethods, allowedMethods.length);
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }
}
