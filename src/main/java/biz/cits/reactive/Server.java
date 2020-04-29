package biz.cits.reactive;

import io.rsocket.*;
import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

public class Server {

    private final Disposable server;

    Logger logger = LoggerFactory.getLogger(Server.class);

    public Server() {
        Resume resume =
                new Resume()
                        .sessionDuration(Duration.ofMinutes(50))
                        .retry(
                                Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(1))
                                        .doBeforeRetry(
                                                retrySignal ->
                                                        logger.debug("Disconnected. Trying to resume connection...")));
        this.server = RSocketServer.create(new SocketAcceptorImpl())
                .resume(resume)
                .bind(TcpServerTransport.create("localhost", 7002))
                .block();

    }

    public void dispose() {
        this.server.dispose();
    }

    private static class SocketAcceptorImpl implements SocketAcceptor {
        @Override
        public Mono<RSocket> accept(ConnectionSetupPayload setupPayload, RSocket reactiveSocket) {
            return Mono.just(
                    new AbstractRSocket() {
                        @Override
                        public Flux<Payload> requestStream(Payload payload) {
                            return Flux.interval(Duration.ofMillis(100))
                                    .map(aLong -> DefaultPayload.create("Interval: " + aLong));
                        }
                    });
        }
    }

}