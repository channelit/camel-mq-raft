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
        fromF("jms:topic:VirtualTopic.%s", outTopic)
                .toF("jms:topic:Consumer.%s.VirtualTopic.%s", client, outTopic);

    }
}
