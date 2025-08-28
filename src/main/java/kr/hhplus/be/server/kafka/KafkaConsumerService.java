package kr.hhplus.be.server.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.event.RestaurantSearchEvent;
import kr.hhplus.be.server.infrastructure.external.DataPlatformApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final DataPlatformApiClient dataPlatformApiClient;

    @KafkaListener(topics = "restaurant-search-events", groupId = "restaurant-search-group")
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Consumed Kafka message: topic={}, key={}, value={}", record.topic(), record.key(), record.value());
        try {
            RestaurantSearchEvent event = objectMapper.readValue(record.value(), RestaurantSearchEvent.class);
            dataPlatformApiClient.sendSearchData(
                    event.getQuery(),
                    event.getUserId(),
                    event.getRestaurants(),
                    event.getDataSource()
            ).subscribe(); // Subscribe to activate the Mono
        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
        }
    }
}
