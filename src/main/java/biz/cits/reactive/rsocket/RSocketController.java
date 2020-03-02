package biz.cits.reactive.rsocket;

import biz.cits.reactive.model.Message;
import biz.cits.reactive.model.MessageRepo;
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
import reactor.core.publisher.Flux;

import javax.jms.TextMessage;
import java.time.Duration;

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

    @MessageMapping("post/{client}")
    public String postMessage(@Payload Message message, @DestinationVariable String client) {
        System.out.println(message);
        jmsTemplate.send(new ActiveMQQueue("in-queue"), messageCreator -> {
            TextMessage textMessage = messageCreator.createTextMessage(message.getMessage());
            textMessage.setStringProperty("client", client);
            textMessage.setJMSCorrelationID(message.getMessage());
            return textMessage;
        });
        return "ok";
    }

    @MessageMapping("posts/{client}")
    public Publisher<Message> postMessage(@Payload Flux<Message> messages, @DestinationVariable String client) {
        return messages.delayElements(Duration.ofMillis(100)).map(message -> {
            jmsTemplate.send(new ActiveMQQueue("in-queue"), messageCreator -> {
                TextMessage textMessage = messageCreator.createTextMessage(message.getMessage());
                textMessage.setStringProperty("client", client);
                textMessage.setJMSCorrelationID(message.getMessage());
                return textMessage;
            });
            return message;
        });

    }

}
