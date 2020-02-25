package biz.biz.cits.reactive.rsocket;

import biz.biz.cits.reactive.model.Message;
import biz.biz.cits.reactive.model.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class RSocketController {

    final
    MessageRepo messageRepo;

    public RSocketController(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @GetMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> getMessages() {
        return messageRepo.getMessages("all");
    }

}
