package kr.hhplus.be.server.application.search;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantTransactionService {

   private final RestaurantRepository repository; 
   private final PopularKeywordRepository keywordRepository;
    
   @Transactional
    public void saveResultsAndUpdateKeywordBlocking(List<Restaurant> restaurants, String query) {
        log.info("DB에 {} 개 결과 저장 및 키워드 카운트 업데이트", restaurants.size());
        repository.save(restaurants);

        String region = keywordRepository.extractRegionFromKeyword(query);
        log.info("추출된 지역: {}", region);

        keywordRepository.increaseCount(query, region);
        log.info("DB 저장 완료");
    } 
}
