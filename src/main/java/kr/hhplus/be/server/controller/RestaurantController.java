package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
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

        List<RestaurantResponse> results = restaurantService.searchRestaurants(request);
        return ResponseEntity.ok(results);
    }

}
