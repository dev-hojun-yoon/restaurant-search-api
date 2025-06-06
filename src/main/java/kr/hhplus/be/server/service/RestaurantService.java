package kr.hhplus.be.server.service;

import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.external.NaverApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

    private final NaverApiClient naverApiClient;

    public RestaurantService(NaverApiClient naverApiClient) {
        this.naverApiClient = naverApiClient;
    }

    public List<RestaurantResponse> searchRestaurants(RestaurantSearchRequest request) {
        return naverApiClient.search(request);
    }
}
