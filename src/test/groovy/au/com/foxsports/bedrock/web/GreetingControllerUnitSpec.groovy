package au.com.foxsports.bedrock.web

import au.com.foxsports.bedrock.config.CommonConfiguration
import au.com.foxsports.bedrock.config.SecurityConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
@IgnoreIf({ !System.properties['demo'] })
@WebFluxTest(controllers = [GreetingController])
@Import([CommonConfiguration, TraceAutoConfiguration, SecurityConfiguration])
@ActiveProfiles(['local', 'demo'])
class GreetingControllerUnitSpec extends Specification {

    @Autowired
    private WebTestClient webTestClient

    void 'test greeting should return correct content'() {
        given:
        def name = 'World'
        def expectedContent = "Bedrock says: Hello, $name!" as String

        expect:
        webTestClient.get()
                .uri('/api/greeting/{name}', name)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath('$.content').isEqualTo(expectedContent)
    }

    void 'test greeting should throw exception and return http #expectedStatus when path name is #name'() {
        expect:
        webTestClient.get()
                .uri('/api/greeting/{name}', name)
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)

        where:
        name       | expectedStatus
        'your-bad' | HttpStatus.INTERNAL_SERVER_ERROR.value()
        'my-bad'   | HttpStatus.BAD_REQUEST.value()
    }
}
