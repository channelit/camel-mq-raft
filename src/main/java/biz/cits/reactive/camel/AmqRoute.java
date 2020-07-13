package biz.cits.reactive.camel;

import biz.cits.reactive.db.DbInjester;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public AmqRoute(@Value("${app.in-topic}") String inTopic, @Value("${app.out-topic}") String outTopic) {
        this.inTopic = inTopic;
        this.outTopic = outTopic;
    }

    @Override
    public void configure() {
        fromF("direct:in-route")
                .to("jms:topic:VirtualTopic." + outTopic + "?disableReplyTo=true")
                .process(exchange -> {
                    JsonNode jsonNode = mapper.readTree(exchange.getMessage().getBody().toString());
                    exchange.getMessage().setMessageId(jsonNode.get("id").asText());
                    exchange.getMessage().setBody(mapper.writeValueAsString(jsonNode));
                })
                .process(new DbInjester())
                .to("jdbc:datasource");
    }
}
