package kr.hhplus.be.server.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class DataPlatformApiClient {

    public Mono<Void> sendSearchData(String query, String userId, List<kr.hhplus.be.server.domain.restaurant.Restaurant> restaurants, String dataSource) {
        return Mono.fromRunnable(() -> {
            log.info("Sending search data to data platform: Query='{}', UserId='{}', RestaurantsCount={}, DataSource='{}'",
                    query, userId, restaurants != null ? restaurants.size() : 0, dataSource);
            // Simulate network delay or processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Data platform API call interrupted", e);
            }
            log.info("Successfully sent search data to data platform.");
        }).then();
    }
}
