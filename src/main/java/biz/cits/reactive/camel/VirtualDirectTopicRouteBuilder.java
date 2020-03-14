package biz.cits.reactive.camel;

import biz.cits.reactive.model.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class VirtualDirectTopicRouteBuilder extends RouteBuilder {
    private final String client;
    private final String outTopic;

    public VirtualDirectTopicRouteBuilder(CamelContext context, String client, String outTopic) {
        super(context);
        this.client = client;
        this.outTopic = outTopic;
    }

    @Override
    public void configure() {
        fromF("jms:queue:Consumer.$s.VirtualTopic.%s", outTopic)
                .process(exchange -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    System.out.println(exchange.getMessage().toString() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                    System.out.println(exchange.getIn().getBody() + "<<<<<<<<<<<<<<<<<<<<<<<BODY<<<<<<");
                    String jsonString = exchange.getMessage().getBody().toString();
                    ClientMessage clientMessage = mapper.readValue(jsonString, ClientMessage.class);
                    exchange.getMessage().setMessageId(clientMessage.getId().toString());
                    exchange.getMessage().setBody(clientMessage);
                })
                .toF("reactive-streams:%s_%s", client, outTopic);

    }
}
