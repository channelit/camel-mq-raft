package biz.cits.reactive.camel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

//TODO: Work in progress. Use virtual route.
public class DurableSuscriberRouteBuilder extends RouteBuilder {
    private String client;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


    public DurableSuscriberRouteBuilder(CamelContext context, String client) {
        super(context);
        this.client = client;
    }

    @Override
    public void configure() {
        from("jms:topic:message-in-topic")
                .to("jms:topic:" + client + "?clientId=" + client + "&durableSubscriptionName=" + client)
                .process(exchange -> {
                    String jsonString = exchange.getIn().getBody().toString();
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = mapper.readTree(jsonString);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    exchange.getMessage().setMessageId(jsonNode.get("id").asText());
                    exchange.getMessage().setBody(jsonString);
                })
                .to("reactive-streams:" + client.toLowerCase() + "-message-out-stream-durable");

    }
}
