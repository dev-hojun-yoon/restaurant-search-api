package kr.hhplus.be.server.domain.keyword;

import java.util.List;
import java.util.Set;

import reactor.core.publisher.Mono;

public interface PopularKeywordRepository {
    void syncKeywordCount(String keyword, String region, Long count);
    Mono<Void> increaseCount(String keyword, String region);
    List<PopularKeyword> findTopKeywords(int limit);
    List<PopularKeyword> findTopKeywordsByRegion(int limit, String region);
    List<PopularKeyword> findAllKeywords();

    void updateKeywordsInRedis(List<PopularKeyword> keywords);
    Set<String> getAllRegions();
    String extractRegionFromKeyword(String keyword);
}