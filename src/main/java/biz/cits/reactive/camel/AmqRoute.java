package biz.cits.reactive.camel;

import biz.cits.reactive.db.DbInjester;
import biz.cits.reactive.model.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AmqRoute extends RouteBuilder {

    static final Logger log = LoggerFactory.getLogger(AmqRoute.class);

    @Override
    public void configure() {
        from("jms:topic:message-in-topic")
                .log(LoggingLevel.DEBUG, log, "in message")
                .process(exchange -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    String jsonString = exchange.getMessage().getBody().toString();
                    ClientMessage clientMessage = mapper.readValue(jsonString, ClientMessage.class);
                    exchange.getMessage().setMessageId(clientMessage.getId().toString());
                    exchange.getMessage().setBody(clientMessage);
                })
                .to("jms:topic:message-out-topic")
                .to("reactive-streams:message-out-stream")
                .process(exchange -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    exchange.getMessage().setBody(mapper.writeValueAsString(exchange.getMessage().getBody()));
                })
                .process(new DbInjester())
                .to("jdbc:datasource");
    }
}
