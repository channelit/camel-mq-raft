package biz.cits.reactive.model;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

@Repository
public class MessageRepo {
    public Flux<Message> getMessages(String filter) {
        return Flux.fromStream(Stream.generate(() -> new Message(filter)))
                .delayElements(Duration.ofMillis(100));
    }
}
