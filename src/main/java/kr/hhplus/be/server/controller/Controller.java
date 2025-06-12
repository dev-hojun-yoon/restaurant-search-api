package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.application.search.PopularKeywordService;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.dto.PopularKeywordRequest;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class Controller {

    private final RestaurantService restaurantService;
    private final PopularKeywordService popularKeywordService;

    public Controller(RestaurantService restaurantService, PopularKeywordService popularKeywordService) {
        this.restaurantService = restaurantService;
        this.popularKeywordService = popularKeywordService;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<Restaurant>> searchRestaurants(
            @RequestParam String query,
            @RequestParam(defaultValue = "random") String sort,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "10") int display
    ) {
        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setQuery(query);
        request.setSort(sort);
        request.setOffset(start);
        request.setSize(display);

        List<Restaurant> results = restaurantService.searchRestaurants(request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/keywords/popular")
    public ResponseEntity<List<PopularKeyword>> searchPopularKeywords(
        @RequestParam(defaultValue = "") String region
    ) {
        PopularKeywordRequest request = new PopularKeywordRequest();
        request.setRegion(region);

        List<PopularKeyword> results = popularKeywordService.findTop(10);
        return ResponseEntity.ok(results);
    }

}