package biz.cits.reactive.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;

public class ReplayRouteBuilder extends RouteBuilder {

    private final String client;
    private CamelContext context;
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private final String query;

    public ReplayRouteBuilder(CamelContext context, String client, String query) {
        super(context);
        this.client = client;
        this.context = context;
        this.query = query;
    }

    @Override
    public void configure() {
        fromF("sql:%s", query)
                .toF("reactive-streams:replay_%s", client).setId("replay_" + client);
    }
}
