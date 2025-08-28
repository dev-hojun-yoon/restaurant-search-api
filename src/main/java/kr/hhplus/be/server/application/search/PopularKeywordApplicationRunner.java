package kr.hhplus.be.server.application.search;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularKeywordApplicationRunner implements ApplicationRunner {

    private final PopularKeywordRepository popularKeywordRepository;
    private final PopularKeywordMemoryService popularKeywordMemoryService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application starting, loading popular keywords from database...");
        try {
            List<PopularKeyword> allKeywords = popularKeywordRepository.findAllKeywords();

            // 1. 메모리 맵 초기화
            popularKeywordMemoryService.initializeMemoryMap(allKeywords);

            // 2. Redis 초기화
            popularKeywordRepository.updateKeywordsInRedis(allKeywords);

            log.info("Successfully loaded and initialized popular keywords.");
        } catch (Exception e) {
            log.error("Failed to initialize popular keywords on application startup", e);
        }
    }
}