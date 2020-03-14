package biz.cits.reactive.camel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class VirtualTopicRouteBuilder extends RouteBuilder {
    private final String client;
    private final String outTopic;

    public VirtualTopicRouteBuilder(CamelContext context, String client, String outTopic) {
        super(context);
        this.client = client;
        this.outTopic = outTopic;
    }

    @Override
    public void configure() {
        fromF("jms:topic:VirtualTopic.%s", outTopic)
                .process(exchange -> {
                    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
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
                .toF("reactive-streams:%s_%s", client, outTopic);
    }
}
