package biz.cits.reactive.model;

import biz.cits.reactive.message.MsgGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

@Repository
public class MessageRepo {

    Logger logger = LoggerFactory.getLogger(MessageRepo.class);

    public Flux<Message> getMessages(String filter) {
        return Flux.fromStream(Stream.generate(() -> new Message(MsgGenerator.getMessages(1).get(0).getValue())).filter(message -> {
            logger.debug("filtering --> " + message.getMessage());
            return message.getMessage().startsWith(filter);
        }))
                .delayElements(Duration.ofMillis(100));
    }
}
