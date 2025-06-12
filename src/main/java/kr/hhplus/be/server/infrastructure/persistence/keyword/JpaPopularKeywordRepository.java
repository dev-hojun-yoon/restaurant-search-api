package kr.hhplus.be.server.infrastructure.persistence.keyword;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPopularKeywordRepository extends JpaRepository<PopularKeywordEntity, String>  {
    Optional<PopularKeywordEntity> findByKeyword(String keyword);
    List<PopularKeywordEntity> findAllByOrderByCountDesc(Pageable pageable);
}
