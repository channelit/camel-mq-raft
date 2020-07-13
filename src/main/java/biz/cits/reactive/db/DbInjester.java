package biz.cits.reactive.db;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbInjester implements Processor {

    static final Logger log = LoggerFactory.getLogger(DbInjester.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        String input = (String) exchange.getMessage().getBody();
        log.debug("Input to be persisted : {}", input);
        String insertQuery = "INSERT INTO messages values ( '" + exchange.getMessage().getMessageId() + "','" + input + "')";
        log.debug("Insert Query is : {}", insertQuery);
        exchange.getIn().setBody(insertQuery);
    }
}