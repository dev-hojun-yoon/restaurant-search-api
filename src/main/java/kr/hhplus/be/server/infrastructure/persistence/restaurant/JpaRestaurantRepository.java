package kr.hhplus.be.server.infrastructure.persistence.restaurant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRestaurantRepository extends JpaRepository<RestaurantEntity, Long>{    
    // title 필드에서 query 문자열을 포함하는 결과를 대소문자 구분 없이 검색
    List<RestaurantEntity> findByTitleContainingIgnoreCase(String query);
}
