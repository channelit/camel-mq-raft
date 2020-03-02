package biz.cits.reactive.rsocket;

import biz.cits.reactive.model.Message;
import biz.cits.reactive.model.MessageRepo;
import io.rsocket.util.DefaultPayload;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.jms.TextMessage;

@Controller
public class RSocketController {

    Logger logger = LoggerFactory.getLogger(RSocketController.class);

    private final JmsTemplate jmsTemplate;

    private final MessageRepo messageRepo;

    private final CamelReactiveStreamsService camel;

    public RSocketController(JmsTemplate jmsTemplate, MessageRepo messageRepo, CamelReactiveStreamsService camel) {
        this.jmsTemplate = jmsTemplate;
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
        return Flux.from(camel.fromStream("messages", Message.class)).filter(message -> message.getMessage().startsWith(filter));
    }

    @MessageMapping("post")
    public String postMessage(@Payload Message message) {
        System.out.println(message);
        jmsTemplate.send(new ActiveMQQueue("in-queue"), messageCreator -> {
            TextMessage textMessage = messageCreator.createTextMessage(message.getMessage());
            textMessage.setJMSCorrelationID(message.getMessage());
            return textMessage;
        });
        return "ok";
    }

}
