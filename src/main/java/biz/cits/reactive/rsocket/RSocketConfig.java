package biz.cits.reactive.rsocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

@Configuration
public class RSocketConfig {

//    @Bean
//    public RSocketStrategies rSocketStrategies() {
//        return RSocketStrategies.builder()
//                .decoder(StringDecoder.textPlainOnly())
//                .encoder(CharSequenceEncoder.allMimeTypes())
//                .routeMatcher(new PathPatternRouteMatcher())
//                .dataBufferFactory(new DefaultDataBufferFactory(true))
//                .build();
//    }
//
//    @Bean
//    public RSocketMessageHandler rSocketMessageHandler() {
//
//        RSocketMessageHandler handler = new RSocketMessageHandler();
//        handler.setRSocketStrategies(rSocketStrategies());
//        return handler;
//
//    }

}
