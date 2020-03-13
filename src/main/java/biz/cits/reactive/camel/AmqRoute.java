package biz.cits.reactive.camel;

import biz.cits.reactive.db.DbInjester;
import biz.cits.reactive.model.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.throttling.ThrottlingInflightRoutePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmqRoute extends RouteBuilder {

    static final Logger log = LoggerFactory.getLogger(AmqRoute.class);

    private final String inTopic;

    private final String outTopic;

    public AmqRoute(@Value("${app.in-topic}") String inTopic, @Value("${app.in-topic}") String outTopic) {
        this.inTopic = inTopic;
        this.outTopic = outTopic;
    }

    @Override
    public void configure() {
        ThrottlingInflightRoutePolicy inflight = new ThrottlingInflightRoutePolicy();
        inflight.setMaxInflightExchanges(2000);
        inflight.setResumePercentOfMax(25);

        fromF("jms:topic:%s", inTopic)
                .log(LoggingLevel.DEBUG, log, "in message")
                .process(exchange -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    String jsonString = exchange.getMessage().getBody().toString();
                    ClientMessage clientMessage = mapper.readValue(jsonString, ClientMessage.class);
                    exchange.getMessage().setMessageId(clientMessage.getId().toString());
                    exchange.getMessage().setBody(clientMessage);
                })
                .toF("jms:topic:VirtualTopic.%s", outTopic)
                .to("reactive-streams:message-out-stream").routePolicy(inflight)
                .process(exchange -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    exchange.getMessage().setBody(mapper.writeValueAsString(exchange.getMessage().getBody()));
                })
                .process(new DbInjester())
                .to("jdbc:datasource");
    }
}
