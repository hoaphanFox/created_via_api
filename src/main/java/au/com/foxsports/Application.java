// Bedrock file. DO NOT DELETE OR MODIFY.

package au.com.foxsports;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;

/**
 * Do not change this class. Any extra configuration required by the application should be placed in its own config
 * class.
 */
@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class Application {

    private final Logger log = LoggerFactory.getLogger(Application.class);

    private final Environment environment;

    private final BuildProperties buildProperties;

    private final GitProperties gitProperties;

    public Application(
            Environment environment,
            @Autowired(required = false) BuildProperties buildProperties,
            @Autowired(required = false) GitProperties gitProperties
    ) {
        this.environment = Objects.requireNonNull(environment);
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startup() {
        String hostAddress;
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
            hostAddress = "localhost";
        }
        String format = "%n" +
                "--------------------------------------------------------------------------------------%n" +
                "    Application %2$s v%8$s (%9$s) is running! Access URLs:%n" +
                "    Local               : %1$s://localhost:%3$s%5$s%n" +
                "    ApiDoc              : %1$s://localhost:%3$s%5$s/api-doc/index.html %n" +
                "    Management          : %1$s://localhost:%4$s%5$s/actuator%n" +
                "    Available Resources : %1$s://localhost:%4$s%5$s/actuator/mappings%n" +
                "    External            : %1$s://%6$s:%4$s%5$s%n" +
                "    Profile(s)          : %7$s%n" +
                "--------------------------------------------------------------------------------------";
        log.info(String.format(
                format,
                environment.getProperty("server.ssl.key-store") == null ? "http" : "https",
                StringUtils.defaultIfBlank(
                        buildProperties == null ? "No Name" : buildProperties.getName(),
                        "NoName"
                ),
                environment.getProperty("server.port"),
                StringUtils.defaultIfBlank(
                        environment.getProperty("management.server.port"),
                        environment.getProperty("server.port")
                ),
                StringUtils.defaultIfBlank(environment.getProperty("server.servlet.context-path"), ""),
                hostAddress,
                Arrays.toString(environment.getActiveProfiles()),
                StringUtils.defaultIfBlank(
                        buildProperties == null ? "NoVersion" : buildProperties.getVersion(),
                        "NoVersion"
                ),
                StringUtils.defaultIfBlank(
                        gitProperties == null ? "NoCommitId" : gitProperties.getShortCommitId(),
                        "NoCommitId"
                )
        ));
    }

    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }
}
