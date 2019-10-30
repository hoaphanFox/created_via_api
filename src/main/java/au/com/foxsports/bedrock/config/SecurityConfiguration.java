package au.com.foxsports.bedrock.config;

import au.com.foxsports.bedrock.security.Auth0JwtDecoder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.apache.commons.lang3.ArrayUtils.addAll;

@Profile("demo")
@Configuration
@EnableConfigurationProperties(Auth0SecurityProperties.class)
public class SecurityConfiguration {

    private static final String[] SYSTEM_URLS = new String[] {
            "/api-doc/**",
            "/webjars/**",
            "/favicon.ico**",
            "/actuator/**"
    };

    private static final String[] PUBLIC_APIS = new String[] {
            "", "/", "/index.html",
            "/api/greeting/**"
    };

    private final Auth0SecurityProperties auth0Properties;

    SecurityConfiguration(final Auth0SecurityProperties auth0Properties) {
        this.auth0Properties = auth0Properties;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        http.headers().cache().disable().and()
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(addAll(SYSTEM_URLS, PUBLIC_APIS)).permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer().jwt()
                .jwtDecoder(new Auth0JwtDecoder(auth0Properties.getDefaultJwtProperties()));
        return http.build();
    }
}
