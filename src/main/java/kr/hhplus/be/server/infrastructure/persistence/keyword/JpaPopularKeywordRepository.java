package kr.hhplus.be.server.infrastructure.persistence.keyword;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPopularKeywordRepository extends JpaRepository<PopularKeywordEntity, Long>  {
    Optional<PopularKeywordEntity> findByKeywordAndRegion(String keyword, String region);
    List<PopularKeywordEntity> findAllByOrderByCountDesc(Pageable pageable);

    // 특정 지역의 인기 키워드 조회.. 위 optional 에서는 부족한지?
    @Query("SELECT p.keyword, p.count FROM PopularKeywordEntity p " + 
           "WHERE p.region = :region ORDER BY p.count DESC")
    List<Object[]> findTopKeywordByRegion(@Param("region") String region, Pageable pageable);

    // (keyword, region) 조합에 반드시 unique 제약조건이 있어야 on duplicate key 가 작동함.
    // ALTER TABLE popular_keyword ADD CONSTRAINT uq_keyword_region UNIQUE(keyword, region);
    @Modifying
    @Query(
        value = "INSERT INTO popular_keyword (keyword, region, count) " + 
                "VALUES (:keyword, :region, 1) " +
                "ON DUPLICATE KEY UPDATE count = count + 1",
        nativeQuery = true
    )
    int upsertKeyword(@Param("keyword") String keyword, @Param("region") String region);
}
