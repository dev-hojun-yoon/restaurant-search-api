package kr.hhplus.be.server.application.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    public void initializeMemoryMap(List<PopularKeyword> dbKeywords) {
        try {
            for (PopularKeyword keyword : dbKeywords) {
                String key = createMapKey(keyword.getKeyword(), keyword.getRegion());
                memoryKeywordMap.put(key, keyword.getCount());
            }
            
            initialized = true;
            log.info("Loaded {} keywords from database to memory", dbKeywords.size());
        } catch (Exception e) {
            log.error("Failed to load keywords from database to memory", e);
        }
    }

    public void increaseCount(String keyword, String region) {
        if (!initialized) {
            log.warn("Memory map is not initialized yet. Skipping increaseCount.");
            return;
        }
        String key = createMapKey(keyword, region);
        memoryKeywordMap.merge(key, 1L, Long::sum);
    }

    // 메모리 데이터를 DB에 동기화 (스케줄러에서 호출)
    @Scheduled(fixedRate = 300000) // 5분
    public void syncToDatabase() {
        if (!initialized || memoryKeywordMap.isEmpty()) {
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

