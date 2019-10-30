package au.com.foxsports.bedrock.web

import au.com.foxsports.bedrock.config.ComponentTest
import au.com.foxsports.bedrock.config.OpenApiValidationSpecification
import org.springframework.http.HttpStatus
import spock.lang.IgnoreIf
import spock.lang.Unroll

@Unroll
@ComponentTest
@IgnoreIf({ !System.properties['demo'] })
class GreetingControllerComponentSpec extends OpenApiValidationSpecification {

    void 'test greeting should return correct content and status'() {
        given:
        def name = 'World'
        def expectedContent = "Bedrock says: Hello, $name!" as String

        expect:
        validatedWebTestClient.get()
                .uri('/api/greeting/{name}', name)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody()
                .jsonPath('$.content').isEqualTo(expectedContent)
    }

    void 'test greeting should throw exception and return http #expectedStatus when path name is #name'() {
        expect:
        validatedWebTestClient.get()
                .uri('/api/greeting/{name}', name)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)

        where:
        name        | expectedStatus
        'your-bad'  | HttpStatus.INTERNAL_SERVER_ERROR
        'my-bad'    | HttpStatus.BAD_REQUEST
    }
}
