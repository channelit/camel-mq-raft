package biz.biz.cits.reactive.rsocket;

import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
public class RSocketConfig {

    @LocalRSocketServerPort
    private int port;

    private URI getURI(RSocketProperties rSocketProps) {
        return URI.create(String.format("ws://localhost:%d%s", port, rSocketProps.getServer().getMappingPath()));
    }


}
