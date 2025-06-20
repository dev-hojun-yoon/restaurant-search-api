package kr.hhplus.be.server.domain.keyword;

import java.util.List;
import java.util.Set;

public interface PopularKeywordRepository {
    void syncKeywordCount(String keyword, String region, Long count);
    // void increaseCount(String keyword, String region);
    List<PopularKeyword> findTopKeywords(int limit);
    List<PopularKeyword> findTopKeywordsByRegion(int limit, String region);
    List<PopularKeyword> findAllKeywords();

    void initializeRegions();
    Set<String> getAllRegions();
    String extractRegionFromKeyword(String keyword);
}
