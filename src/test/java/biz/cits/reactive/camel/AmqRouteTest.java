package biz.cits.reactive.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@RunWith(CamelSpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"spring.activemq.broker-url=vm://localhost?broker.persistent=false"})
public class AmqRouteTest {

    @Autowired
    protected CamelContext camelContext;

    protected MockEndpoint mockB;

    protected JmsEndpoint endpoint;

    @EndpointInject(value = "mock:c")
    protected MockEndpoint mockC;

    @Produce("jms:queue:in-topic")
    protected ProducerTemplate start2;

    @EndpointInject(value = "mock:log:biz.cits.reactive")
    protected MockEndpoint mockLog;

    @Test
    public void testPositive() throws Exception {

        mockC.expectedBodiesReceived("David");
        mockLog.expectedBodiesReceived("Hello David");

        start2.sendBody("David");

        MockEndpoint.assertIsSatisfied(camelContext);
    }


}
