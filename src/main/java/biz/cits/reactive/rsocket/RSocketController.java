package biz.cits.reactive.rsocket;

import biz.cits.reactive.model.Message;
import biz.cits.reactive.model.MessageRepo;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RSocketController {

    Logger logger = LoggerFactory.getLogger(RSocketController.class);

    final MessageRepo messageRepo;

    private final CamelReactiveStreamsService camel;

    public RSocketController(MessageRepo messageRepo, CamelReactiveStreamsService camel) {
        this.messageRepo = messageRepo;
        this.camel = camel;
    }

    @MessageMapping("messages/{filter}")
    public Publisher<Message> getMessages(@DestinationVariable String filter) {
        logger.debug("FILTER -----> " + filter);
        return messageRepo.getMessages(filter);
    }

    @MessageMapping("camel/{filter}")
    public Publisher<Message> getCamel(@DestinationVariable String filter) {
        return camel.fromStream("messages", Message.class);
    }

}
