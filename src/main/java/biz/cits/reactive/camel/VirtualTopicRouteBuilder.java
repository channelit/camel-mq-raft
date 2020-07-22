package biz.cits.reactive.camel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.transaction.annotation.Transactional;

public class VirtualTopicRouteBuilder extends RouteBuilder {
    private final String client;
    private final String outTopic;
    private final String id;
    private final CamelContext context;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public VirtualTopicRouteBuilder(CamelContext context, String client, String outTopic, String id) {
        super(context);
        this.client = client;
        this.outTopic = outTopic;
        this.id = id;
        this.context = context;
    }

    @Override
    public void configure() {
        if (context.getRoute(client) == null) {
            fromF("activemq:queue:Consumer.%s.VirtualTopic.%s", client, outTopic)
                    .routeId(client + "_" + id)
                    .onException(IllegalStateException.class).markRollbackOnly().end()
                    .transacted()
                    .process(exchange -> {
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
                    .doTry()
                        .toF("reactive-streams:%s_%s_%s?maxInflightExchanges=100", client, outTopic, id)
                    .doCatch(IllegalStateException.class)
                        .process(exchange -> {
//                            exchange.getContext().getRoute(client + "_" + id).getEndpoint().stop();
//                            exchange.getContext().getRoute(client + "_" + id).getConsumer().stop();
                            log.info("Exit Route " + client + " " + context.getRoutes().toString());
                            throw new IllegalStateException();
                        })
                    .end();
        } else {
            Route route = context.getRoute(client);
            route.getEndpoint().start();
            route.getConsumer().start();
        }
    }
}
