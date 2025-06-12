package kr.hhplus.be.server.infrastructure.persistence.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRestaurantRepository extends JpaRepository<RestaurantEntity, Long>{    
}
