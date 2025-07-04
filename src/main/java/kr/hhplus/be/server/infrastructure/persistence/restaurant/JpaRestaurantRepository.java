package kr.hhplus.be.server.infrastructure.persistence.restaurant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRestaurantRepository extends JpaRepository<RestaurantEntity, Long> {    
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO restaurant (title, category, road_address, mapx, mapy)
            VALUES (:title, :category, :roadAddress, :mapx, :mapy)
            ON DUPLICATE KEY UPDATE
                title = VALUES(title),
                category = VALUES(category),
                mapx = VALUES(mapx),
                mapy = VALUES(mapy)
            """, nativeQuery = true)
    void upsertRestaurant(
        @Param("roadAddress") String roadAddress,
        @Param("title") String title,
        @Param("category") String category,
        @Param("mapx") double mapx,
        @Param("mapy") double mapy
    );

    Optional<RestaurantEntity> findByRoadAddress(String roadAddress);
    
    // title 필드에서 query 문자열을 포함하는 결과를 대소문자 구분 없이 검색
    // List<RestaurantEntity> findByTitleContainingIgnoreCase(String query);

    @Query(
        value = "SELECT * FROM restaurant WHERE title LIKE %:query%",
        nativeQuery = true
    )
    List<RestaurantEntity> searchByFullText(@Param("query") String query);

}
