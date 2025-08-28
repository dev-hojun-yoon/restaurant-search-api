package kr.hhplus.be.server.application.search;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopularKeywordService {
    private final PopularKeywordRepository popularKeywordRepository;
    private final PopularKeywordMemoryService memoryService;

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<PopularKeyword> findTop(int limit) {
        return popularKeywordRepository.findTopKeywords(limit);
    }

    public List<PopularKeyword> findTopByRegion(int limit, String region) {
        return popularKeywordRepository.findTopKeywordsByRegion(limit, region);
    }

    public List<String> getAllRegions() {
        return new ArrayList<>(popularKeywordRepository.getAllRegions());
    }

    public void increaseCount(String keyword, String region) {
        // 메모리 맵 카운트 증가
        memoryService.increaseCount(keyword, region);
        // Redis 카운트 증가 (비동기)
        popularKeywordRepository.increaseCount(keyword, region).subscribe();
    }

    public String extractRegionFromKeyword(String keyword) {
        return popularKeywordRepository.extractRegionFromKeyword(keyword);
    }
}