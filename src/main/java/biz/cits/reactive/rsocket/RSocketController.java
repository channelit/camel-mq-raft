package biz.cits.reactive.rsocket;

import biz.cits.reactive.model.Message;
import biz.cits.reactive.model.MessageRepo;
import org.reactivestreams.Publisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RSocketController {

    final
    MessageRepo messageRepo;

    public RSocketController(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @MessageMapping("messages")
    public Publisher<Message> feedMarketData() {
        return messageRepo.getMessages("world");
    }

}
