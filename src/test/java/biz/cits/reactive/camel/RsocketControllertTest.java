package biz.cits.reactive.camel;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
public class RsocketControllertTest extends AbstractRSocketTest{

    @Test
    @DisplayName("JSON Parser Test")
    public void JsonErrorTest() {
        RSocketRequester requester = createRSocketRequester();
        Flux<String> response = requester.route("camel-virtual/test/test").data("Invalid JSON").retrieveFlux(String.class);
        StepVerifier.create(response)
                .expectNext("hello world")
                .expectComplete()
                .verify();


    }
}
