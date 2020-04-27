package biz.cits.reactive.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReplayRouteBuilder extends RouteBuilder {

    private final String client;
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private final String query;
    private final CamelContext context;

    public ReplayRouteBuilder(CamelContext context, String client, String query) {
        super(context);
        this.client = client;
        this.query = query;
        this.context = context;
    }

    @Override
    public void configure() {
        fromF("timer://timer_%s?repeatCount=1&delay=1000", client)
                .setBody(constant(query))
                .to("jdbc:datasource?outputType=StreamList")
                .split(body()).streaming()
                .process(exchange -> {
                    ArrayList bodyList = exchange.getMessage().getBody(ArrayList.class);
                    Map<String, Object> studentMap = (Map<String, Object>) bodyList.get(0);
                    exchange.getIn().setBody(studentMap.get("message"));
                })
                .toF("reactive-streams:replay_%s", client)
                .end()
                .setId("replay_" + client);
    }
}
