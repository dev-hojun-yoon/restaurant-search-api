package kr.hhplus.be.server.infrastructure.persistence.keyword;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaKeyRegionRepository extends JpaRepository<KeyRegionEntity, Long> {
    @Query("SELECT k.regionName FROM KeyRegionEntity k")
    List<String> findAllRegionNames();

    boolean existsByRegionName(String regionName);
}
