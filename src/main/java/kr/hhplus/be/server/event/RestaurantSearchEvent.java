package kr.hhplus.be.server.event;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class RestaurantSearchEvent extends ApplicationEvent {
    private final String query;
    private final String userId;
    private final List<Restaurant> restaurants;
    private final String dataSource;

    public RestaurantSearchEvent(Object source, String query, String userId, List<Restaurant> restaurants, String dataSource) {
        super(source);
        this.query = query;
        this.userId = userId;
        this.restaurants = restaurants;
        this.dataSource = dataSource;
    }
}
