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
    private CamelContext context;

    public VirtualTopicRouteBuilder(CamelContext context, String client, String outTopic) {
        super(context);
        this.client = client;
        this.outTopic = outTopic;
        this.context = context;
    }

    @Override
    public void configure() {
        if (!context.getRoutes().contains(client)) {
            fromF("jms:queue:Consumer.%s.VirtualTopic.%s", client, outTopic)
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
                        exchange.getMessage().setBody(jsonNode);
                    })
                    .toF("reactive-streams:%s_%s", client, outTopic).id(client);
        }
    }
}
