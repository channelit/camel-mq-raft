package biz.cits.reactive.camel;

import biz.cits.reactive.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Configuration;

public class ReplayRouteBuilder extends RouteBuilder {

    private final String client;
    private final String outTopic;
    private CamelContext context;
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public ReplayRouteBuilder(CamelContext context, String client, String outTopic) {
        super(context);
        this.client = client;
        this.outTopic = outTopic;
        this.context = context;
    }

    @Override
    public void configure() throws Exception {
        from("timer://replay?period=1000")
                .setBody(constant("select message from messages"))
                .to("log:?level=INFO&showBody=true")
                .to("reactive-streams:replay");
    }
}
