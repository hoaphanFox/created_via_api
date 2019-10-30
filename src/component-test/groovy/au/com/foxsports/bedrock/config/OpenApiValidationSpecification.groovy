package au.com.foxsports.bedrock.config

import com.atlassian.oai.validator.wiremock.ValidatedWireMockRule
import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.Rule
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Shared
import spock.lang.Specification

class OpenApiValidationSpecification extends Specification {

    private static final String TARGET_URL = 'targetUrl'
    private static final String DEFAULT_TARGET_URL = 'http://localhost:8080'

    @Shared
    WebTestClient validatedWebTestClient

    @Rule
    ValidatedWireMockRule validatedWireMockRule = new ValidatedWireMockRule("$targetUrl/api-doc/definition.yaml", 0)

    void setup() {
        validatedWireMockRule.stubFor(WireMock.any(WireMock.anyUrl()).willReturn(WireMock.aResponse().proxiedFrom(targetUrl)))
        validatedWebTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:${validatedWireMockRule.port()}").build()
    }

    private String getTargetUrl() {
        System.getProperty(TARGET_URL) ?: DEFAULT_TARGET_URL
    }
}
