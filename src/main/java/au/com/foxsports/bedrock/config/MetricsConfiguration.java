package au.com.foxsports.bedrock.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

@Configuration
public class MetricsConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
            final  Environment environment,
            @Value("${project.name}") final String projectName) {

        return registry -> registry.config().commonTags(asList(
                Tag.of("host", getHostname()),
                Tag.of("service", projectName),
                Tag.of("activeProfiles", StringUtils.join(environment.getActiveProfiles(), ","))));
    }

    private String getHostname() {
        // AWS instances will have the HOSTNAME env variable - otherwise fallback to getting the IP
        return ofNullable(System.getenv("HOSTNAME"))
                .orElseGet(this::getHostnameFromNetwork);
    }

    private String getHostnameFromNetwork() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
