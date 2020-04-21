package biz.cits.reactive.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(CamelSpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = {"classpath:application.yml"})
@MockEndpoints("jms:*")
public class AmqRouteTest {

    @Autowired
    protected CamelContext camelContext;

    protected MockEndpoint mockB;

    @EndpointInject(value = "mock:c")
    protected MockEndpoint mockC;

    @Produce("direct:start2")
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
