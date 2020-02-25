package biz.biz.cits.reactive.model;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Random;
import java.util.stream.Stream;

@Repository
public class MessageRepo {
    public Flux<Message> getMessages(String filter) {
        Random r = new Random();
        int low = 0;
        int high = 50;
        return Flux.fromStream(Stream.generate(() -> r.nextInt(high - low) + low)
                .map(String::valueOf)
                .peek(System.out::println))
                .map(Message::new)
                .delayElements(Duration.ofSeconds(1));
    }
}
