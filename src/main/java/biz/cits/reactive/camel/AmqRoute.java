package biz.cits.reactive.camel;

import biz.cits.reactive.db.DbInjester;
import biz.cits.reactive.model.Message;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AmqRoute extends RouteBuilder {

    static final Logger log = LoggerFactory.getLogger(AmqRoute.class);

    @Override
    public void configure() {
        from("jms:in-queue")
                .convertBodyTo(Message.class)
                .to("reactive-streams:messages")
                .log(LoggingLevel.DEBUG, log, "in message")
                .process(exchange -> {
                    String convertedMessage = exchange.getMessage().getBody() + " new";
                    exchange.getMessage().setBody(convertedMessage);
                })
                .to("jms:out-queue")
                .process(new DbInjester())
                .to("jdbc:datasource");
    }
}
