package biz.biz.cits.reactive.camel;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AmqRoute extends RouteBuilder {

    static final Logger log = LoggerFactory.getLogger(AmqRoute.class);

    @Override
    public void configure() throws Exception {
        from("jms:in-queue")
                .log(LoggingLevel.DEBUG, log, "in message")
                .process(exchange -> {
                    String convertedMessage = exchange.getMessage().getBody() + " new";
                    exchange.getMessage().setBody(convertedMessage);
                })
                .to("jms:out-queue");

    }
}
