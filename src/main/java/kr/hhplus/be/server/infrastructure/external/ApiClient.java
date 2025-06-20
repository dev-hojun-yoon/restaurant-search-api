package kr.hhplus.be.server.infrastructure.external;

import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import reactor.core.publisher.Mono;

public interface ApiClient {
    Mono<ApiCallResult> search(RestaurantSearchRequest request);
}
