package kr.hhplus.be.server.integration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.infrastructure.persistence.restaurant.JpaRestaurantRepository;

@Component
public class TestDbHelper {
    
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private JpaRestaurantRepository jpaRestaurantRepository;
    
    // 테스트 데이터 생성 헬퍼 메서드
    @Transactional
    public void createTestRestaurants() {
        restaurantRepository.save(
            List.of(
                new Restaurant("불고기 맛집", "한식", "서울 강남구", "123", "456"),
                new Restaurant("불고기 전문점", "한식", "서울 서초구", "789", "012")
            )
        );
    }

    @Transactional
    public void cleanUp() {
        jpaRestaurantRepository.deleteAll();
    }
}
