package biz.cits.reactive.camel;

import biz.cits.reactive.model.ClientMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class VirtualTopicRouteBuilder extends RouteBuilder {
    private String client;

    public VirtualTopicRouteBuilder(CamelContext context, String client) {
        super(context);
        this.client = client;

    }

    @Override
    public void configure() throws Exception {
        from("jms:topic:message-in-topic")
                .to("jms:topic:VirtualTopic." + client)
                .process(exchange -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    String jsonString = exchange.getMessage().getBody().toString();
                    ClientMessage clientMessage = mapper.readValue(jsonString, ClientMessage.class);
                    exchange.getMessage().setMessageId(clientMessage.getId().toString());
                    exchange.getMessage().setBody(clientMessage);
                })
                .to("reactive-streams:" + client.toLowerCase() + "-message-out-stream-virtual");

    }
}
