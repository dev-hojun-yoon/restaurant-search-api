package kr.hhplus.be.server.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.event.RestaurantSearchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendRestaurantSearchEvent(RestaurantSearchEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("restaurant-search-events", event.getQuery(), message);
            log.info("Produced Kafka message: topic={}, key={}, message={}", "restaurant-search-events", event.getQuery(), message);
        } catch (JsonProcessingException e) {
            log.error("Error serializing RestaurantSearchEvent to JSON", e);
        }
    }
}
