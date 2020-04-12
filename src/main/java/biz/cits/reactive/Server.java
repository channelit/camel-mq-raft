package biz.cits.reactive;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.TcpServerTransport;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Server {
    private final Disposable server;

    public Server() {
        this.server = RSocketFactory.receive()
                .acceptor((setupPayload, reactiveSocket) -> Mono.just(new RSocketImpl()))
                .transport(TcpServerTransport.create("localhost", 7001))
                .start()
                .subscribe();
    }

    public void dispose() {
        this.server.dispose();
    }

    private class RSocketImpl extends AbstractRSocket {

        @Override
        public Flux<Payload> requestStream(Payload payload) {
            String streamName = payload.getDataUtf8();

//            if (DATA_STREAM_NAME.equals(streamName)) {
//                return Flux.from(dataPublisher);
//            }
            return Flux.error(new IllegalArgumentException(streamName));
        }


    }
}