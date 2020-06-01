package biz.cits.reactive.rsocket;

import biz.cits.reactive.camel.DurableSuscriberRouteBuilder;
import biz.cits.reactive.camel.ReplayRouteBuilder;
import biz.cits.reactive.camel.VirtualTopicRouteBuilder;
import biz.cits.reactive.model.ClientMessageRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import static net.logstash.logback.argument.StructuredArguments.kv;

import javax.jms.TextMessage;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.IntStream;

@Controller
public class RSocketController {

    Logger log = LoggerFactory.getLogger(RSocketController.class);

    private final String inTopic;

    private final String outTopic;

    private JmsTemplate jmsTemplate;

    private final ClientMessageRepo messageRepo;

    private final CamelReactiveStreamsService camel;

    private final CamelContext camelContext;

    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public RSocketController(@Value("${app.in-topic}") String inTopic, @Value("${app.out-topic}") String outTopic, JmsTemplate jmsTemplate, ClientMessageRepo clientMessageRepo, CamelReactiveStreamsService camel, CamelContext camelContext, @Value("${app.jms-ttl-millis}") long ttl) {
        this.inTopic = inTopic;
        this.outTopic = outTopic;
        this.jmsTemplate = jmsTemplate;
        jmsTemplate.setTimeToLive(ttl);
        this.messageRepo = clientMessageRepo;
        this.camel = camel;
        this.camelContext = camelContext;
    }

    @MessageMapping("")
    public Publisher<String> routeMessage(@Payload String message, RSocketRequester requester) throws Exception {
        log.info("FILTER:{}", message);
        String filter, route, client, data;
        try {
            JsonNode node = mapper.readTree(message);
            filter = node.get("filter").asText();
            route = node.get("route").asText();
            client = node.get("client").asText();
            data = node.get("data").asText();
            log.info("route message client={}", kv("appId", client));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            ObjectNode response = mapper.createObjectNode();
            response.put("error", e.getMessage());
            return Flux.just(response.asText());
        }
        switch (route) {
            case "subscribe":
                return getCamelVirtual(client, filter);
            case "post":
                return postMessage(data, client);
            case "replay":
                return replay(client, filter, data);
            default:
                return getMessages(filter);
        }
    }

    @MessageMapping("messages/{filter}")
    public Flux<String> getMessages(@DestinationVariable String filter) {
        log.info("FILTER:{}", filter);
        return messageRepo.getMessages(filter);
    }

    @MessageMapping("camel/{filter}")
    public Publisher<String> getCamel(@DestinationVariable String filter) {
        return Flux.from(camel.fromStream("message_out_stream", String.class)).filter(message -> applyFilter(message, filter));
    }

    @MessageMapping("camel-durable/{client}/{filter}")
    public Publisher<String> getCamelDurable(@DestinationVariable String client, @DestinationVariable String filter) {
        try {
            camelContext.addRoutes(new DurableSuscriberRouteBuilder(camelContext, client));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Flux.from(camel.fromStream(client.toLowerCase() + "-message-out-stream-durable", String.class)).filter(message -> applyFilter(message, filter));
    }

    //TODO: Works with only single connection. Use Virtual instead.
    @MessageMapping("camel-durable-direct/{client}/{filter}")
    public Publisher<String> getCamelDurableDirect(@DestinationVariable String client, @DestinationVariable String filter) {
        return Flux.from(camel.from("jms:topic:message-out-topic?clientId=" + client + "&cacheLevelName=CACHE_CONSUMER&subscriptionDurable=true&durableSubscriptionName=" + client, String.class));
    }

    @MessageMapping("camel-virtual-direct/{client}/{filter}")
    public Publisher<String> getCamelVirtualDirect(@DestinationVariable String client, @DestinationVariable String filter) {
        return Flux.from(camel.from("jms:queue:Consumer." + client + ".VirtualTopic." + outTopic, String.class)).filter(message -> applyFilter(message, filter)).delayElements(Duration.ofMillis(100));
    }

    @MessageMapping("camel-virtual/{client}/{filter}")
    public Publisher<String> getCamelVirtual(@DestinationVariable String client, @DestinationVariable String filter) throws Exception {
        camelContext.addRoutes(new VirtualTopicRouteBuilder(camelContext, client, outTopic));
        return Flux.from(camel.fromStream(client + "_" + outTopic, String.class)).filter(message -> applyFilter(message, filter))
                .delayElements(Duration.ofMillis(100))
                .doOnCancel(() -> terminateRoute(client, "cancel"))
                .doOnTerminate(() -> terminateRoute(client, "terminate"))
                .doOnError(error -> terminateRoute(client, "error"));
    }

    @MessageMapping("replay/{client}/{filter}")
    public Publisher<String> replay(@DestinationVariable String client, @DestinationVariable String filter, @Payload String jsonQuery) throws Exception {
        terminateRoute("replay_" + client, "route_start");
        camelContext.addRoutes(new ReplayRouteBuilder(camelContext, client, jsonQuery));
        return Flux.from(camel.fromStream("replay_" + client, String.class)).filter(message -> applyFilter(message, filter)).doOnCancel(() -> terminateRoute("replay_" + client, "cancel")).doOnComplete(() -> terminateRoute("replay_" + client, "complete")).doOnComplete(
                () -> {
                    System.out.println("Done");
                }
        );
    }

    private void terminateRoute(String routeId, String event) {
        try {
            Route route = camelContext.getRoute(routeId);
            route.getConsumer().stop();
            route.getEndpoint().stop();
            log.info("Route stopped {} {}", kv("appId", routeId), kv("event", event));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("post/{client}")
    public Publisher<String> postMessage(@Payload String message, @DestinationVariable String client) {
//        log.debug("received {}", kv("received-message", message));
        return Flux.just(generateMessage(message, client));
    }


    private String generateMessage(String message, String client) {
        ObjectNode response = mapper.createObjectNode();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(message);
            jmsTemplate.send(new ActiveMQQueue(inTopic), messageCreator -> {
                TextMessage textMessage = messageCreator.createTextMessage(message);
                textMessage.setStringProperty("client", client);
                textMessage.setJMSExpiration(5000);
                textMessage.setJMSCorrelationID(jsonNode.get("id").asText());
                log.info("{},{}", kv("client", client), kv("messageId", textMessage.getJMSCorrelationID()));
                return textMessage;
            });
            response.put("message", "ok");
        } catch (JmsException | JsonProcessingException e) {
            e.printStackTrace();
            response.put("error", e.getMessage());
        }
        return response.toString();
    }

    @MessageMapping("posts/{client}")
    public Publisher<String> postMessage(@Payload Flux<String> messages, @DestinationVariable String client) {
        return messages.delayElements(Duration.ofMillis(100)).map(message -> generateMessage(message, client));
    }

    private boolean applyFilter(String message, String filter) {
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String[] filters = filter.split(",");
        String client = jsonNode.get("client").asText();
        return Arrays.asList(filters).contains(client);
    }

}
