// Bedrock file. DO NOT DELETE OR MODIFY.

package au.com.foxsports.bedrock.config;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class CommonConfiguration {

    private final ServerProperties serverProperties;

    public CommonConfiguration(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    /*
     * Default java.time.ZoneId instance for beans that need it.
     *
     */
    @Bean
    public ZoneId zoneId() {
        return ZoneId.of("Australia/Sydney");
    }

    /*
     * Default java.time.Clock instance for beans that need it.
     *
     */
    @Bean
    public Clock clock() {
        return Clock.system(zoneId());
    }

    /*
     * Jackson Afterburner module to speed up serialization/deserialization.
     *
     */
    @Bean
    @Profile("json-afterburner")
    public AfterburnerModule afterburnerModule() {
        return new AfterburnerModule();
    }

    /*
     * Jackson Module for serialization/deserialization of Problem (RFC7807) instances.
     */
    @Bean
    ProblemModule problemModule() {
        ProblemModule problemModule = new ProblemModule();
        if (serverProperties.getError().getIncludeStacktrace() == ErrorProperties.IncludeStacktrace.ALWAYS) {
            return problemModule.withStackTraces();
        }
        return problemModule;
    }

    /*
     * Jackson Module for serialization/deserialization of Problem (RFC7807) instances derived from Bean
     * Validation (JSR380) constraint violations.
     */
    @Bean
    ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }

    @Bean
    static PropertySourcesPlaceholderConfigurer gitPropertiesConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }
}
