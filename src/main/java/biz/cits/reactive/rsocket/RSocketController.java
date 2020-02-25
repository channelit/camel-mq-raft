package biz.cits.reactive.rsocket;

import biz.cits.reactive.model.Message;
import biz.cits.reactive.model.MessageRepo;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RSocketController {

    final
    MessageRepo messageRepo;

    public RSocketController(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @MessageMapping({"messages/{filter}"})
    public Publisher<Message> feedMarketData(@PathVariable String filter) {
        return messageRepo.getMessages(filter);
    }

}
