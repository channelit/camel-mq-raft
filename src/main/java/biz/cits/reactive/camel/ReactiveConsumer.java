package biz.cits.reactive.camel;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;

@Configuration
public class ReactiveConsumer {

    @Component
    public static class CamelToReactive {
        private static final Logger LOG = LoggerFactory.getLogger(CamelToReactive.class);

        @Autowired
        private CamelReactiveStreamsService camel;
//
//
//        @PostConstruct
//        public void setupStreams() {
//
//            Publisher<String> messages = camel.fromStream("messages", String.class);
//
//            Flux.from(messages)
//                    .map(tuple -> "BasicCamelToReactor - " + tuple.toString())
//                    .doOnNext(LOG::info)
//                    .subscribe();
//        }

    }

}
