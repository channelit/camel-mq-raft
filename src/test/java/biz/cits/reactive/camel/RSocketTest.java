package biz.cits.reactive.camel;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;

@RunWith(SpringRunner.class)
public class RSocketTest {

    @TestConfiguration
    static class ClientConfig {

//        @Bean
//        WebClient getWebClient() {
//            return WebClient.create("http://localhost:7000");
//        }

        @Bean
        RSocket rsocket() {
            return RSocketFactory.connect()
                    .mimeType("/messages", MimeTypeUtils.APPLICATION_JSON_VALUE)
                    .frameDecoder(PayloadDecoder.ZERO_COPY)
                    .transport(TcpClientTransport.create(new InetSocketAddress("localhost", 7001)))
                    .start()
                    .block();
        }


    }


//    @Autowired
//    WebClient webClient;

    @Autowired
    RSocket rSocket;

    @Test
    public void Test() {
//        webClient.get()
//                .uri("/messages")
//                .accept(MediaType.APPLICATION_STREAM_JSON)
//                .retrieve()
//                .bodyToFlux(Message.class)
//                .map(String::valueOf)
//                .subscribe(System.out::println);

        Flux<Payload> s = rSocket.requestStream(DefaultPayload.create("messages"));
        s.take(10).doOnNext(p -> System.out.println(p.getDataUtf8())).blockLast();
    }
}
