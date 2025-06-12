package kr.hhplus.be.server.service;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    private final NaverApiClient naverApiClient;
    private final RestaurantRepository repository;

    public RestaurantService(NaverApiClient naverApiClient, RestaurantRepository repository) {
        this.naverApiClient = naverApiClient;
        this.repository = repository;
    }

    public List<Restaurant> searchRestaurants(RestaurantSearchRequest request) {
        List<Restaurant> externalApiResponse = naverApiClient.search(request);
        // 검색 기록 저장
        repository.save(externalApiResponse);

        return naverApiClient.search(request);
    }
}
