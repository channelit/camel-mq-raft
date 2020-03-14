package biz.cits.reactive.model;

import biz.cits.reactive.message.MsgGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class ClientMessageRepo {

    Logger logger = LoggerFactory.getLogger(ClientMessageRepo.class);

    public Flux<String> getMessages(String filter) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return Flux.fromStream(Stream.generate(() -> {
            Map.Entry<String, String> message = MsgGenerator.getMessages(1).get(0);

            ClientMessage clientMessage = ClientMessage.builder()
                    .client(message.getKey())
                    .id(UUID.randomUUID())
                    .content(message.getValue())
                    .messageDateTime(Instant.now()).build();

            String jsonString = "";
            try {
                jsonString = mapper.writeValueAsString(clientMessage);

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return jsonString;
        }).filter(message -> {
            ClientMessage clientMessage = null;
            try {
                clientMessage = mapper.readValue(message, ClientMessage.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            logger.debug("filtering --> " + clientMessage.getClient());
            return clientMessage.getClient().startsWith(filter);
        })).delayElements(Duration.ofMillis(100));
    }
}
