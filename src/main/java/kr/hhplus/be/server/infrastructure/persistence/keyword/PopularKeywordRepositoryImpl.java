package kr.hhplus.be.server.infrastructure.persistence.keyword;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Repository
@Slf4j
public class PopularKeywordRepositoryImpl implements PopularKeywordRepository{
    
    private final JpaPopularKeywordRepository jpaPopularKeywordRepository;
    private final JpaKeyRegionRepository jpaKeyRegionRepository;
    private final Set<String> regionCache = ConcurrentHashMap.newKeySet();
    private volatile boolean initialized = false;

    @Override
    @Transactional
    public void syncKeywordCount(String keyword, String region, Long count) {
        PopularKeywordEntity entity = jpaPopularKeywordRepository.findByKeywordAndRegion(keyword, region)
                                        .orElseGet(() -> new PopularKeywordEntity(keyword, region));

        entity.setCount(count);
        jpaPopularKeywordRepository.save(entity);
    }

    @Override
    public List<PopularKeyword> findAllKeywords() {
        return jpaPopularKeywordRepository.findAll().stream()
                .map(entity -> new PopularKeyword(
                    entity.getKeyword(),
                    entity.getCount(),
                    entity.getRegion()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void increaseCount(String keyword, String region) {
        int affectedRows = jpaPopularKeywordRepository.updatKeywordCount(keyword, region);
        if (affectedRows == 0) {
            // row 가 없을 시 Insert
            jpaPopularKeywordRepository.save(new PopularKeywordEntity(keyword, region));
        }
        // PopularKeywordEntity entity = jpaPopularKeywordRepository.findByKeywordAndRegion(keyword, region)
        //                         .orElseGet(() -> new PopularKeywordEntity(keyword, region));

        // entity.increaseCount();
                                
        // // Id 를 PK 값으로 지정하는 것은 어떤지 검토 필요
        // if (entity.getKeyword() == null) {
        //     jpaPopularKeywordRepository.save(entity);
        // }
    }

    @Override
    public List<PopularKeyword> findTopKeywords(int limit) {
        return jpaPopularKeywordRepository.findAllByOrderByCountDesc(PageRequest.of(0 , limit)).stream()
                .map(e -> new PopularKeyword(e.getKeyword(), e.getCount()))
                .toList();
    }

    @Override
    public List<PopularKeyword> findTopKeywordsByRegion(int limit, String region) {
        List<Object[]> results = jpaPopularKeywordRepository.findTopKeywordByRegion(region, PageRequest.of(0, limit));

        return results.stream()
                .map(result -> new PopularKeyword(
                    (String) result[0],
                    ((Number) result[1]).longValue(),
                    region))
                .toList();
    }

    
    @PostConstruct
    public void initializeRegions() {
        try {
            // 초기 데이터 설정
            List<String> defaultRegions = Arrays.asList("강남", "홍대", "정자", "성수");

            for (String region : defaultRegions) {
                if (!jpaKeyRegionRepository.existsByRegionName(region)) {
                    jpaKeyRegionRepository.save(new KeyRegionEntity(region));
                }
            }

            // 메모리 캐시 로드
            loadRegionsToMemory();
            log.info("key regions initialized: {}", regionCache);
        } catch (Exception e) {
            log.error("Failed to initialize key regions", e);
        }
    }

    private void loadRegionsToMemory() {
        regionCache.clear();
        regionCache.addAll(jpaKeyRegionRepository.findAllRegionNames());
        initialized = true;
    }

    public Set<String> getAllRegions() {
        if (!initialized) {
            loadRegionsToMemory();
        }
        return new HashSet<>(regionCache);
    }

    public String extractRegionFromKeyword(String keyword) {
        if (!initialized) {
            loadRegionsToMemory();
        }

        return regionCache.stream()
                .filter(keyword::contains)
                .findFirst()
                .orElse(null);
    }
    
}
