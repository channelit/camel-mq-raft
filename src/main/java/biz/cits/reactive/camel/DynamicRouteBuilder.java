package biz.cits.reactive.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class DynamicRouteBuilder extends RouteBuilder {
    private final String from;
    private final String to;

    public DynamicRouteBuilder(CamelContext context, String from, String to) {
        super(context);
        this.from = from;
        this.to = to;
    }

    @Override
    public void configure() throws Exception {
        from(from).to(to).id(from + "-" + to);
    }
}
