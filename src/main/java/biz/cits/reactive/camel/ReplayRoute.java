package biz.cits.reactive.camel;

import biz.cits.reactive.model.Message;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReplayRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
//        from("timer://replay?period=1000")
//                .setBody(constant("select message from messages"))
//                .to("log:?level=INFO&showBody=true")
//                .to("reactive-streams:replay");

    }
}
