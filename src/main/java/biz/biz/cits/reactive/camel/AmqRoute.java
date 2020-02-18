package biz.biz.cits.reactive.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AmqRoute extends RouteBuilder {


    @Override
    public void configure() throws Exception {
        from("activemq:in-queue")
                .to("log:amq-log");

        from("timer:from-out")
                .setBody(constant("Hello World!"))
                .to("activemq:in-queue");

    }
}
