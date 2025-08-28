package kr.hhplus.be.server.infrastructure.persistence.keyword;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
@Slf4j
public class PopularKeywordRepositoryImpl implements PopularKeywordRepository {

    private final JpaPopularKeywordRepository jpaPopularKeywordRepository;
    private final JpaKeyRegionRepository jpaKeyRegionRepository;
    private final StringRedisTemplate redisTemplate;

    private final Set<String> regionCache = ConcurrentHashMap.newKeySet();
    private volatile boolean initialized = false;
    private final Object initLock = new Object();

    private static final String REDIS_KEY_PREFIX = "search:ranking:";
    private static final Duration TTL = Duration.ofDays(3);

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
    public void updateKeywordsInRedis(List<PopularKeyword> keywords) {
        try {
            for (PopularKeyword keyword : keywords) {
                String redisKey = REDIS_KEY_PREFIX + keyword.getRegion();
                redisTemplate.opsForZSet().add(redisKey, keyword.getKeyword(), keyword.getCount());
                redisTemplate.expire(redisKey, TTL);
            }
            log.info("Updated {} keywords in Redis", keywords.size());
        } catch (Exception e) {
            log.error("Failed to update keywords in Redis", e);
        }
    }

    @Override
    public Mono<Void> increaseCount(String keyword, String region) {
        return Mono.fromRunnable(() -> {
            try {
                String redisKey = REDIS_KEY_PREFIX + region;
                redisTemplate.opsForZSet().incrementScore(redisKey, keyword, 1);
                redisTemplate.expire(redisKey, TTL);
            } catch (Exception e) {
                log.error("Failed to increase count in Redis for keyword: {}, region: {}", keyword, region, e);
            }
        });
    }

    @Override
    public List<PopularKeyword> findTopKeywords(int limit) {
        return jpaPopularKeywordRepository.findAllByOrderByCountDesc(PageRequest.of(0, limit)).stream()
                .map(e -> new PopularKeyword(e.getKeyword(), e.getCount(), e.getRegion()))
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

    @Override
    public Set<String> getAllRegions() {
        if (!initialized) {
            loadRegionsToMemory();
        }
        return new HashSet<>(regionCache);
    }

    @Override
    public String extractRegionFromKeyword(String keyword) {
        if (!initialized) {
            synchronized (initLock) {
                if (!initialized) {
                    loadRegionsToMemory();
                }
            }
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }

        return regionCache.stream()
                .filter(keyword::contains)
                .findFirst()
                .orElse("");
    }

    private void loadRegionsToMemory() {
        regionCache.clear();
        regionCache.addAll(jpaKeyRegionRepository.findAllRegionNames());
        initialized = true;
    }
}