package au.com.foxsports.bedrock.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class ComponentTestConfiguration implements WebFluxConfigurer {

    private static final String TARGET_URL = 'targetUrl'
    private static final String DEFAULT_TARGET_URL = 'http://localhost:8080'

    @Bean
    WebTestClient webTestClient() {
        WebTestClient.bindToServer().baseUrl(targetUrl).build()
    }

    private String getTargetUrl() {
        System.getProperty(TARGET_URL) ?: DEFAULT_TARGET_URL
    }
}
