package biz.cits.reactive.db;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbReplay implements Processor {
    private static Logger log = LoggerFactory.getLogger(DbReplay.class);
    @Override
    public void process(Exchange exchange) throws Exception {
        String input = (String) exchange.getIn().getBody();
        log.debug("Input to be persisted : " + input);
        String selectQuery = "SELECT * FROM messages";
        log.debug("select Query is : " + selectQuery);
        exchange.getIn().setBody(selectQuery);
    }
}
