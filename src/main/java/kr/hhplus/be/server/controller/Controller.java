package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.application.search.PopularKeywordService;
import kr.hhplus.be.server.application.search.RestaurantService;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.dto.PopularKeywordRequest;
import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import reactor.core.publisher.Mono;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/search")
public class Controller {

    private final RestaurantService restaurantService;
    private final PopularKeywordService popularKeywordService;

    public Controller(RestaurantService restaurantService, PopularKeywordService popularKeywordService) {
        this.restaurantService = restaurantService;
        this.popularKeywordService = popularKeywordService;
    }

    /*
     * 맛집 검색 API (비동기 처리)
     * @param request 검색 요청 파라미터를 담은 DTO  (@ModelAttribute 사용)
     * @return 맛집 검색 결과 리스트를 담은 Mono<ResponseEntity>
     */
    @GetMapping("/restaurants")
    public Mono<ResponseEntity<RestaurantResponse>> searchRestaurants(
            @ModelAttribute RestaurantSearchRequest request
    ) {
        // 서비스에서 Mono<RestaurantResponse> 를 반환하므로,
        // 그 결과를 받아 ResponseEntity 를 감싸주면 된다.
        return restaurantService.searchRestaurants(request)
                .map(ResponseEntity::ok); // .map(response -> ResponseEntity.ok(response)) 와 동일한 의미
    }

    @GetMapping("/keywords/popular")
    public ResponseEntity<List<PopularKeyword>> searchPopularKeywords(
        @RequestParam(defaultValue = "10") int limit) {
            List<PopularKeyword> results = popularKeywordService.findTop(10);
            return ResponseEntity.ok(results);
    }

    @GetMapping("/keywords/popular/region")
    public ResponseEntity<List<PopularKeyword>> searchPopularKeywordsByRegion(
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam String region) {
            List<PopularKeyword> results = popularKeywordService.findTopByRegion(limit, region);
            return ResponseEntity.ok(results);
    }
}