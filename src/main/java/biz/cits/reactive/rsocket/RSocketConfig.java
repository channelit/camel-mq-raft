package biz.cits.reactive.rsocket;

import io.rsocket.RSocketFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.rsocket.netty.NettyRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.boot.rsocket.server.ServerRSocketFactoryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;

import java.util.stream.Collectors;

@Configuration
public class RSocketConfig {

    @Bean
    ReactorResourceFactory reactorResourceFactory() {
        return new ReactorResourceFactory();
    }

    @Bean
    ServerRSocketFactoryProcessor serverRSocketFactoryProcessor() {
        return RSocketFactory.ServerRSocketFactory::resume;
    }

    @Bean
    RSocketServerFactory rSocketServerFactory(RSocketProperties properties, ReactorResourceFactory reactorResourceFactory,
                                              ObjectProvider<ServerRSocketFactoryProcessor> processors) {
        NettyRSocketServerFactory factory = new NettyRSocketServerFactory();
        factory.setResourceFactory(reactorResourceFactory);
        factory.setTransport(properties.getServer().getTransport());
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(properties.getServer().getAddress()).to(factory::setAddress);
        map.from(properties.getServer().getPort()).to(factory::setPort);
        factory.setSocketFactoryProcessors(processors.orderedStream().collect(Collectors.toList()));
        return factory;
    }
}
