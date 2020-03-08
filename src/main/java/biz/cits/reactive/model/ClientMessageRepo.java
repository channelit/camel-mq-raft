package biz.cits.reactive.model;

import biz.cits.reactive.message.MsgGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class ClientMessageRepo {

    Logger logger = LoggerFactory.getLogger(ClientMessageRepo.class);

    public Flux<ClientMessage> getMessages(String filter) {
        return Flux.fromStream(Stream.generate(() -> {
            Map.Entry<String, String> message = MsgGenerator.getMessages(1).get(0);
            return ClientMessage.builder()
                    .client(message.getKey())
                    .id(UUID.randomUUID())
                    .content(message.getValue())
                    .messageDateTime(Instant.now()).build();
        }).filter(message -> {
            logger.debug("filtering --> " + message.getClient());
            return message.getClient().startsWith(filter);
        })).delayElements(Duration.ofMillis(100));
    }
}
