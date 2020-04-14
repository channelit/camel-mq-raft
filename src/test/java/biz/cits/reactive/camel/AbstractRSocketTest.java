package biz.cits.reactive.camel;

import io.rsocket.transport.netty.client.TcpClientTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;

abstract class AbstractRSocketTest {
    @Value("${spring.rsocket.server.port}")
    private int serverPort;

    @Autowired
    private RSocketRequester.Builder builder;

    RSocketRequester createRSocketRequester() {
        return builder.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .connect(TcpClientTransport.create(serverPort)).block();
    }

}
