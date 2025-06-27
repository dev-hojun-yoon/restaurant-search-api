package kr.hhplus.be.server.infrastructure.persistence.restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RestaurantRepositoryImpl implements RestaurantRepository{

    private final JpaRestaurantRepository jpaRepository;

    // @Override
    // public void save(List<Restaurant> restaurants) {
    //     List<RestaurantEntity> entities = restaurants.stream()
    //         .map(RestaurantEntity::fromDomain)
    //         .collect(Collectors.toList());

    //     jpaRepository.saveAll(entities);
    // }
    // @Override
    // public void save(List<Restaurant> restaurants) {
    //     List<RestaurantEntity> upsertEntities = new ArrayList<>();

    //     for (Restaurant restaurant : restaurants) {
    //         String roadAddress = restaurant.getAddress();
    //         Optional<RestaurantEntity> existing = jpaRepository.findByRoadAddress(roadAddress);

    //         RestaurantEntity entity = existing
    //                                     .map(e -> e.updateFromDomain(restaurant))
    //                                     .orElse(RestaurantEntity.fromDomain(restaurant));

    //         upsertEntities.add(entity);
    //     }

    //     jpaRepository.saveAll(upsertEntities);
    // }
    @Override
    public void save(List<Restaurant> restaurants) {
        for (Restaurant restaurant : restaurants) {
            jpaRepository.upsertRestaurant(
                restaurant.getAddress(),
                restaurant.getTitle(),
                restaurant.getCategory(),
                restaurant.getMapx(),
                restaurant.getMapy()
            );
        }
    }

    // DB 에서 쿼리로 맛집을 검색하는 기능의 구현부
    @Override
    public List<Restaurant> findByQuery(String query) {
        // 1. JpaRepository 를 호출하여 Entity 리스트를 가져옴
        List<RestaurantEntity> entities = jpaRepository.searchByFullText(query);

        // 2. Entity 리스트를 Domain (DTO) 객체 리스트로 변환
        return entities.stream()
                .map(RestaurantEntity::toDomain) // 엔티티를 도메인 객체로 변환
                .collect(Collectors.toList());
    }    
}
