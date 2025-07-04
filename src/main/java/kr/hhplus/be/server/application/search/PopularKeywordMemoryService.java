package kr.hhplus.be.server.application.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularKeywordMemoryService {
    
    private final PopularKeywordRepository popularKeywordRepository;
    
    // 메모리 맵: "keyword|region" -> count
    private final ConcurrentHashMap<String, Long> memoryKeywordMap = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    @PostConstruct
    public void initialize() {
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        try {
            List<PopularKeyword> dbKeywords = popularKeywordRepository.findAllKeywords();
            
            for (PopularKeyword keyword : dbKeywords) {
                String key = createMapKey(keyword.getKeyword(), keyword.getRegion());
                memoryKeywordMap.put(key, keyword.getCount());
            }
            
            initialized = true;
            log.info("Loaded {} keywords from database to memory", memoryKeywordMap.size());
        } catch (Exception e) {
            log.error("Failed to load keywords from database", e);
        }
    }

    // 키워드 카운트 증가 (메모리에서)
    public void increaseCount(String keyword) {
        String region = popularKeywordRepository.extractRegionFromKeyword(keyword);
        String mapKey = createMapKey(keyword, region);

        memoryKeywordMap.merge(mapKey, 1L, Long::sum);
        log.debug("Increased keyword count in memory: {} -> {}", mapKey, memoryKeywordMap.get(mapKey));
    }

    // 메모리 데이터를 DB에 동기화 (스케줄러에서 호출)
    @Scheduled(fixedRate = 300000) // 5분
    public void syncToDatabase() {
        if (!initialized | memoryKeywordMap.isEmpty()) {
            return;
        }

        try {
            log.info("Starting sync to database. Memory map size: {}", memoryKeywordMap.size());

            Map<String, Long> snapshot = new HashMap<>(memoryKeywordMap);

            for (Map.Entry<String, Long> entry : snapshot.entrySet()) {
                String[] keyParts = parseMapKey(entry.getKey());
                String keyword = keyParts[0];
                String region = keyParts[1];
                Long count = entry.getValue();

                popularKeywordRepository.syncKeywordCount(keyword, region, count);
            }

            log.info("Database sync completed successfully");
            
        } catch (Exception e) {
            log.error("Failed to sync keywords to database", e);
        }
    }

    private String createMapKey(String keyword, String region) {
        return keyword + "|" + (region != null ? region : "");
    }

    private String[] parseMapKey(String mapKey) {
        String[] parts = mapKey.split("\\|", 2);
        String keyword = parts[0];
        String region = parts.length > 1 && !parts[1].isEmpty() ? parts[1] : null;
        return new String[]{keyword, region};
    }
}
