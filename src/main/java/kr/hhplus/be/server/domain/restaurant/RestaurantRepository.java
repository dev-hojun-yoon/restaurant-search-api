package kr.hhplus.be.server.domain.restaurant;

import java.util.List;

public interface RestaurantRepository {
    void save(List<Restaurant> restaurants);
}
