package biz.cits.reactive.rsocket;

import biz.cits.reactive.model.ClientMessage;
import biz.cits.reactive.model.Message;
import biz.cits.reactive.model.ClientMessageRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.CamelContext;
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

    private final ClientMessageRepo messageRepo;

    private final CamelReactiveStreamsService camel;

    private final CamelContext camelContext;

    public RSocketController(JmsTemplate jmsTemplate, ClientMessageRepo clientMessageRepo, CamelReactiveStreamsService camel, CamelContext camelContext) {
        this.jmsTemplate = jmsTemplate;
        this.messageRepo = clientMessageRepo;
        this.camel = camel;
        this.camelContext = camelContext;
    }

    @MessageMapping("messages/{filter}")
    public Flux<ClientMessage> getMessages(@DestinationVariable String filter) {
        logger.debug("FILTER -----> " + filter);
        return messageRepo.getMessages(filter);
    }

    @MessageMapping("camel/{filter}")
    public Publisher<ClientMessage> getCamel(@DestinationVariable String filter) {
        return Flux.from(camel.fromStream("messages", ClientMessage.class)).filter(message -> message.getClient().startsWith(filter));
    }

    @MessageMapping("replay/{client}")
    public Publisher<ClientMessage> replay(@DestinationVariable String client) throws Exception {
        return Flux.from(camel.fromStream("replay", ClientMessage.class));
    }

    @MessageMapping("post/{client}")
    public String postMessage(@Payload Message message, @DestinationVariable String client) {
        System.out.println(message);
        jmsTemplate.send(new ActiveMQQueue("in-queue"), messageCreator -> {
            ObjectMapper mapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            mapper.registerModule(module);
            TextMessage textMessage = null;
            try {
                textMessage = messageCreator.createTextMessage(mapper.writeValueAsString(message));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            textMessage.setStringProperty("client", client);
            textMessage.setJMSCorrelationID(message.toString());
            return textMessage;
        });
        return "ok";
    }

    @MessageMapping("posts/{client}")
    public Publisher<ClientMessage> postMessage(@Payload Flux<ClientMessage> messages, @DestinationVariable String client) {
        return messages.delayElements(Duration.ofMillis(100)).map(message -> {
            jmsTemplate.send(new ActiveMQQueue("in-queue"), messageCreator -> {
                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                TextMessage textMessage = null;
                try {
                    textMessage = messageCreator.createTextMessage(mapper.writeValueAsString(message));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                textMessage.setStringProperty("client", client);
                textMessage.setJMSCorrelationID(message.getId().toString());
                return textMessage;
            });
            return message;
        });
    }

}
