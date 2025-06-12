package kr.hhplus.be.server.infrastructure.persistence.restaurant;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RestaurantRepositoryImpl implements RestaurantRepository{

    private final JpaRestaurantRepository jpaRepository;

    @Override
    public void save(List<Restaurant> restaurants) {
        List<RestaurantEntity> entities = restaurants.stream()
            .map(RestaurantEntity::fromDomain)
            .collect(Collectors.toList());

        jpaRepository.saveAll(entities);
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'save'");
    }
    
}
