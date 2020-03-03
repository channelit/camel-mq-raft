package biz.cits.reactive.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class DbRouteBuilder extends RouteBuilder {
    private final String from;
    private final String to;

    public DbRouteBuilder(CamelContext context, String from, String to) {
        super(context);
        this.from = from;
        this.to = to;
    }
    @Override
    public void configure() throws Exception {
        from(from).to(to);
    }
}
