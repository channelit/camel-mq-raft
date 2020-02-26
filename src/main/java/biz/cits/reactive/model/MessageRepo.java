package biz.cits.reactive.model;

import biz.cits.reactive.message.MsgGenerator;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

@Repository
public class MessageRepo {
    public Flux<Message> getMessages(String filter) {
        return Flux.fromStream(Stream.generate(() -> new Message(MsgGenerator.getMessages(1).get(0).getValue())).filter(message -> {
            System.out.println("filtering --> " + message.getMessage());
            return message.getMessage().startsWith(filter);
        }))
                .delayElements(Duration.ofMillis(100));
    }
}
