package kr.hhplus.be.server.domain.restaurant;

import java.util.List;

public interface RestaurantRepository {
    void save(List<Restaurant> restaurants);

    // DB 에서 쿼리로 맛집을 검색하는 메서드 추가
    List<Restaurant> findByQuery(String query);
}
