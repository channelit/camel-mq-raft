package biz.cits.reactive.camel;

import biz.cits.reactive.rsocket.RSocketController;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = {"classpath:application.yml"})
@SpringBootTest
public class RsocketControllertTest {

    @Autowired
    private RSocketRequester rSocketRequester;

    @SpyBean
    private RSocketController rSocketController;

    @Autowired
    private JmsTemplate mockJmsTemplate;

    @Test
    @DisplayName("JSON Parser Error Test")
    public void JsonErrorTest() throws Exception {
        Mono<String> response = rSocketRequester.route("post/me").data("{Invalid JSON}").retrieveMono(String.class);
//        verify(rSocketController).getCamelVirtual(any(), any());
        String expected = "{\"error\":\"Unrecognized token 'Invalid': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false') at [Source: (String)\\\"Invalid JSON\\\"; line: 1, column: 8]\"})";
        StepVerifier.create(response)
                .expectNextCount(1)
                .expectComplete()
                .verify();


    }

    @TestConfiguration
    public static class ClientConfiguration {

        @Bean
        @Lazy
        public RSocket rSocket() {
            return RSocketFactory.connect()
                    .mimeType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.toString(), WellKnownMimeType.APPLICATION_CBOR.toString())
                    .frameDecoder(PayloadDecoder.ZERO_COPY)
                    .transport(TcpClientTransport.create(7000))
                    .start()
                    .block();
        }

        @Bean
        @Lazy
        RSocketRequester rSocketRequester(RSocketStrategies strategies) {
            return RSocketRequester.builder()
                    .rsocketFactory(factory -> factory
                            .dataMimeType(MimeTypeUtils.ALL_VALUE)
                            .frameDecoder(PayloadDecoder.ZERO_COPY))
                    .rsocketStrategies(strategies)
                    .connect(TcpClientTransport.create("localhost", 7000))
                    .retry().block();
        }
    }
}
