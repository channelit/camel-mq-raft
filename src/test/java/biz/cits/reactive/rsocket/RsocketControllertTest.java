package biz.cits.reactive.rsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestPropertySource(locations = {"classpath:application.yml"})
public class RsocketControllertTest {

    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Autowired
    private RSocketRequester rSocketRequester;

    @Autowired
    private JmsTemplate mockJmsTemplate;

    @Test
    @DisplayName("JSON Parser Error Test")
    public void JsonErrorTest() throws Exception {
        Mono<String> response = rSocketRequester.route("post/me").data("Invalid JSON").retrieveMono(String.class).map(this::parseJson);
        StepVerifier.create(response)
                .expectNext("True")
                .expectComplete()
                .verify();


    }

    private String parseJson(String m) {
        try {
            JsonNode jsonNode = mapper.readTree(m);
            return jsonNode.has("error") ? "True" : "False";
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "False";
        }
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
