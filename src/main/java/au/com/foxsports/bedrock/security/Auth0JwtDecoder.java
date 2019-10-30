package au.com.foxsports.bedrock.security;

import au.com.foxsports.bedrock.config.Auth0SecurityProperties.JwtProperties;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.trimToNull;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.springframework.security.oauth2.jwt.JwtClaimNames.ISS;
import static org.springframework.util.Assert.notNull;
import static reactor.core.publisher.Mono.just;

public class Auth0JwtDecoder implements ReactiveJwtDecoder {

    private static final String IETF_URI = "https://tools.ietf.org/html/rfc6750#section-3.1";

    private final NimbusJwtDecoderJwkSupport nimbusJwtDecoderJwkSupport;

    public Auth0JwtDecoder(final JwtProperties jwtProperties) {
        this.nimbusJwtDecoderJwkSupport = new NimbusJwtDecoderJwkSupport(jwtProperties.getJwkSetUri());
        this.nimbusJwtDecoderJwkSupport.setJwtValidator(validators(jwtProperties));
    }

    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        return just(nimbusJwtDecoderJwkSupport.decode(token));
    }

    private DelegatingOAuth2TokenValidator<Jwt> validators(final JwtProperties jwtProperties) {
        return new DelegatingOAuth2TokenValidator<>(Arrays.asList(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(jwtProperties.getIssuer()),
                new JwtAudienceValidator(jwtProperties.getAllowedAudiences())));
    }

    private static class JwtAudienceValidator implements OAuth2TokenValidator<Jwt> {

        private static OAuth2Error INVALID_AUDIENCE = new OAuth2Error(INVALID_REQUEST,
                "The Claim 'aud' value doesn't contain the required audience", IETF_URI);

        private final List<String> allowedAudience;

        public JwtAudienceValidator(final List<String> allowedAudience) {
            notNull(allowedAudience, "audience cannot be null");
            this.allowedAudience = allowedAudience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(final Jwt token) {
            notNull(token, "token cannot be null");
            final List<String> audience = token.getAudience();
            if (audience == null || !allowedAudience.stream().anyMatch(allowed -> audience.contains(allowed))) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error[]{INVALID_AUDIENCE});
            }
            return OAuth2TokenValidatorResult.success();
        }
    }

    private static class JwtIssuerValidator implements OAuth2TokenValidator<Jwt> {

        private static OAuth2Error INVALID_ISSUER = new OAuth2Error(INVALID_REQUEST,
                "This iss claim is not equal to the configured issuer", IETF_URI);

        private final String expectedIssuer;

        public JwtIssuerValidator(final String expectedIssuer) {
            notNull(trimToNull(expectedIssuer), "issuer cannot be null");
            this.expectedIssuer = expectedIssuer;
        }

        @Override
        public OAuth2TokenValidatorResult validate(final Jwt token) {
            notNull(token, "token cannot be null");
            return expectedIssuer.equals(token.getClaimAsString(ISS)) ? OAuth2TokenValidatorResult.success() :
                    OAuth2TokenValidatorResult.failure(new OAuth2Error[]{INVALID_ISSUER});
        }
    }
}
