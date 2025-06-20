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
    @Query("SELECT p.keyword, p.count FROM PopularKeywordEntity p" + 
           "WHERE p.region = :region ORDER BY p.count DESC")
    List<Object[]> findTopKeywordByRegion(String region, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PopularKeywordEntity p SET p.count = p.count + 1" + 
           "WHERE p.keyword = :keyword AND p.region = :region")
    int updatKeywordCount(@Param("keyword") String keyword, @Param("region") String region);
}
