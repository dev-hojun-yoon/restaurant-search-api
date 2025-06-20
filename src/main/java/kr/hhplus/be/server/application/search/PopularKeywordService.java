package kr.hhplus.be.server.application.search;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopularKeywordService {
    private final PopularKeywordRepository popularKeywordRepository;
    private final PopularKeywordMemoryService memoryService;

    public List<PopularKeyword> findTop(int limit) {
        return popularKeywordRepository.findTopKeywords(limit);
    }

    public List<PopularKeyword> findTopByRegion(int limit, String region) {
        return popularKeywordRepository.findTopKeywordsByRegion(limit, region);
    }

    public List<String> getAllRegions() {
        return new ArrayList<>(popularKeywordRepository.getAllRegions());
    }

    public void increaseCount(String keyword) {
        memoryService.increaseCount(keyword);
    }
}
