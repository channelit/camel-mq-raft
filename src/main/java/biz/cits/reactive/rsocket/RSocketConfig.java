package biz.cits.reactive.rsocket;

import biz.cits.reactive.Server;
import io.rsocket.RSocketFactory;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import io.rsocket.resume.InMemoryResumableFramesStore;
import io.rsocket.resume.ResumableDuplexConnection;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.stream.Collectors;

@Configuration
public class RSocketConfig {

    Logger logger = LoggerFactory.getLogger(RSocketConfig.class);

    @Bean
    Resume resume() {
        return new Resume()
                .sessionDuration(Duration.ofSeconds(1))
                .cleanupStoreOnKeepAlive()
                .storeFactory(t -> new InMemoryResumableFramesStore("server", 500_000));
    }

    @Bean
    public RSocketMessageHandler rsocketMessageHandler() {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRouteMatcher(new PathPatternRouteMatcher());
        return handler;
    }

    @Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
                .routeMatcher(new PathPatternRouteMatcher())
                .build();
    }

    @Bean
    CloseableChannel closeableChannel() {
        return
                RSocketServer.create()
                        .resume(resume())
                        .acceptor(rsocketMessageHandler().responder())
                        .bind(TcpServerTransport.create("localhost", 7000))
                        .cache()
                        .block();
    }

//    @Bean
//    RSocketServerFactory rSocketServerFactory(RSocketProperties properties, ReactorResourceFactory reactorResourceFactory,
//                                              ObjectProvider<ServerRSocketFactoryProcessor> processors) {
//        NettyRSocketServerFactory factory = new NettyRSocketServerFactory();
//        factory.setResourceFactory(reactorResourceFactory);
//        factory.setTransport(properties.getServer().getTransport());
//        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
//        map.from(properties.getServer().getAddress()).to(factory::setAddress);
//        map.from(properties.getServer().getPort()).to(factory::setPort);
//        factory.setSocketFactoryProcessors(processors.orderedStream().collect(Collectors.toList()));
//        return factory;
//    }
}
