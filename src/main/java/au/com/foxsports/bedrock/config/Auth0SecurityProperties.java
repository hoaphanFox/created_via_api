package au.com.foxsports.bedrock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@ConfigurationProperties(prefix = "auth0security")
public class Auth0SecurityProperties {

    private Map<String, JwtProperties> jwt;

    private JwtProperties defaultJwtProperties;

    @PostConstruct
    public void postConstruct() {
        defaultJwtProperties = jwtPropertiesFor("default");
    }

    private JwtProperties jwtPropertiesFor(final String name) {
        return ofNullable(jwt.get(name)).orElseThrow(() ->
                new IllegalStateException("JwtProperties not found for " + name));
    }

    public Map<String, JwtProperties> getJwt() {
        return this.jwt;
    }

    public JwtProperties getDefaultJwtProperties() {
        return this.defaultJwtProperties;
    }

    public void setJwt(Map<String, JwtProperties> jwt) {
        this.jwt = jwt;
    }

    public void setDefaultJwtProperties(JwtProperties defaultJwtProperties) {
        this.defaultJwtProperties = defaultJwtProperties;
    }

    public static class JwtProperties {

        private String issuer;

        private List<String> allowedAudiences;

        private String jwkSetUri;

        public String getIssuer() {
            return this.issuer;
        }

        public List<String> getAllowedAudiences() {
            return this.allowedAudiences;
        }

        public String getJwkSetUri() {
            return this.jwkSetUri;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public void setAllowedAudiences(List<String> allowedAudiences) {
            this.allowedAudiences = allowedAudiences;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }
    }
}
