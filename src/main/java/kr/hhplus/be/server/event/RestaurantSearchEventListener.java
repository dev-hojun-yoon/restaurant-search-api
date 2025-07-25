package kr.hhplus.be.server.event;

import kr.hhplus.be.server.infrastructure.external.DataPlatformApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantSearchEventListener {

    private final DataPlatformApiClient dataPlatformApiClient;

    @Async
    @EventListener
    public void handleRestaurantSearchEvent(RestaurantSearchEvent event) {
        log.info("Handling RestaurantSearchEvent for query: {}", event.getQuery());
        dataPlatformApiClient.sendSearchData(
                event.getQuery(),
                event.getUserId(),
                event.getRestaurants(),
                event.getDataSource()
        ).subscribe(); // Subscribe to activate the Mono
    }
}
