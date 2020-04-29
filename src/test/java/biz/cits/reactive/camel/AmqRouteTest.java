package biz.cits.reactive.camel;

import biz.cits.reactive.message.MsgGenerator;
import biz.cits.reactive.model.ClientMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;

import javax.jms.TextMessage;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
@TestPropertySource(properties = {"spring.activemq.broker-url=vm://localhost?broker.persistent=false", "spring.rsocket.server.port=7001", "spring.rsocket.server.address=localhost"})
public class AmqRouteTest {

    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected CamelReactiveStreamsService camel;

    protected MockEndpoint mockB;

    @Autowired
    protected JmsTemplate jmsTemplate;

    @EndpointInject(value = "mock:c")
    protected MockEndpoint mockC;

    @Produce("jms:queue:in-topic")
    protected ProducerTemplate start;

    @Produce("jms:topic:VirtualTopic.out-topic")
    protected ProducerTemplate end;

    private String inTopic = "in-topic";

    @Test
    public void testPositive() throws Exception {

        Map.Entry<String, String> message = MsgGenerator.getMessages(1).get(0);

        ClientMessage clientMessage = ClientMessage.builder()
                .client(message.getKey())
                .id(UUID.randomUUID())
                .content(message.getValue())
                .messageDateTime(Instant.now()).build();

        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(clientMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        final String messageString = jsonString;
        jmsTemplate.send(new ActiveMQQueue(inTopic), messageCreator -> {
            TextMessage textMessage = messageCreator.createTextMessage(messageString);
            textMessage.setStringProperty("client", "client");
            return textMessage;
        });
        CountDownLatch receptionLatch = new CountDownLatch(1);
        MockEndpoint.assertIsSatisfied(camelContext);
        Flux.from(camel.from("reactive-streams:message_out_stream")).subscribe(
                m -> {
                    receptionLatch.countDown();
                }
        );
        Assert.assertThat(receptionLatch.getCount(), Matchers.equalTo(1L));

    }


}
