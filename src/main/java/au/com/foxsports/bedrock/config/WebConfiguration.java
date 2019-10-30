package au.com.foxsports.bedrock.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableConfigurationProperties(WebConfigProperties.class)
public class WebConfiguration {

    private final WebConfigProperties webConfigProperties;

    public WebConfiguration(final WebConfigProperties webConfigProperties) {
        this.webConfigProperties = webConfigProperties;
    }

    @Bean
    WebFluxConfigurer webFluxConfigurer() {
        return new WebFluxConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                final WebConfigProperties.Cors cors = webConfigProperties.getCors();
                registry.addMapping("/**")
                        .allowedOrigins(cors.getAllowedOrigins())
                        .allowedMethods(cors.getAllowedMethods())
                        .maxAge(cors.getMaxAge());
            }
        };
    }
}
