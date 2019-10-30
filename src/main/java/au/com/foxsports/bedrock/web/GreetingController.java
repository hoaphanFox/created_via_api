// Bedrock file. DO NOT DELETE OR MODIFY.

package au.com.foxsports.bedrock.web;

import au.com.foxsports.bedrock.model.Greeting;
import brave.Span;
import brave.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
@Profile("demo")
class GreetingController {

    private final Logger log = LoggerFactory.getLogger(GreetingController.class);

    private final AtomicLong counter = new AtomicLong();

    private final Tracer tracer;

    public GreetingController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping(value = "/greeting/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Greeting> greeting(
            @PathVariable(name = "name") String name,
            @MatrixVariable(value = "namespace", required = false, defaultValue = "Bedrock") String namespace
    ) {
        final Span span = tracer.nextSpan().name("greeting").start();

        return Mono.just(name)
                .doOnNext(n -> span.annotate("This is a greeting span!"))
                .doOnNext(n -> span.tag("nameParam", n))
                .doOnNext(this::validateName)
                .map(n -> counter.incrementAndGet())
                .doOnNext(count -> span.tag("requestCount", Long.toString(count)))
                .map(count -> new Greeting(
                        count,
                        String.format("%s says: Hello, %s!", namespace == null ? "Default" : namespace, name)))
                .doOnNext(greeting -> log.info("Will return: {}", greeting))
                .doAfterSuccessOrError((g, t) -> span.finish());
    }


    private void validateName(final String name) {
        if ("your-bad".equalsIgnoreCase(name)) {
            throw new GreetingInternalServerErrorException();
        }

        if ("my-bad".equalsIgnoreCase(name)) {
            throw new GreetingBadRequestException();
        }
    }
}
