package biz.cits.reactive.db;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class DbReplay implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String input = (String) exchange.getIn().getBody();
        System.out.println("Input to be persisted : " + input);
        String selectQuery = "SELECT * FROM messages";
        System.out.println("select Query is : " + selectQuery);
        exchange.getIn().setBody(selectQuery);
    }
}
