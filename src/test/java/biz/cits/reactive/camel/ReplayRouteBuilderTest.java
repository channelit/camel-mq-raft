package biz.cits.reactive.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ReplayRouteBuilderTest {

    @Autowired
    private CamelContext camelContext;


    @Test
    public void testReplayRouteBuilder() throws Exception {
        camelContext.addRoutes(new ReplayRouteBuilder(camelContext, "abcde", "select * from something"));
        Assert.assertNotNull(camelContext.getRoute("replay_abcde"));
    }
}
