package biz.cits.reactive.rsocket;

import biz.cits.reactive.model.Message;
import biz.cits.reactive.model.MessageRepo;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RSocketController {

    final
    MessageRepo messageRepo;

    public RSocketController(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @MessageMapping("messages/{filter}")
    public Publisher<Message> getMessages(@DestinationVariable String filter) {
        System.out.println("FILTER -----> " + filter);
        return messageRepo.getMessages(filter);
    }

}
