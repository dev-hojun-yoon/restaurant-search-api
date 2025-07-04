package kr.hhplus.be.server.infrastructure.external;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriComponentsBuilder;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.config.ApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractApiClient<T, P extends ApiProperties> implements ApiClient {

    private final WebClient webClient;
    protected final P properties;

    // API 호출 상태 코드 및 에러 처리 
    @Override
    public Mono<ApiCallResult> search(RestaurantSearchRequest request) {
        return Mono.fromCallable(() -> {
            // URI 구성
            // UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(properties.getBaseUrl());
            // UriComponentsBuilder detailedUri = buildUriDetails(uriBuilder, request);
            // return detailUri.toUriString();
            return buildUri(request, properties.getBaseUrl());
        })
        .flatMap(uri -> {
            HttpHeaders headers = new HttpHeaders();
            addHeaders(headers);

            log.info("{} API call start: {}", getApiName(), uri);

            return webClient.get()
                        .uri(uri)
                        .headers(httpHeaders -> httpHeaders.addAll(headers))
                        .exchangeToMono(response -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(getResponseDtoClass())
                                    .map(this::toRestaurants)
                                    .map(restaurants -> {
                                        restaurants.forEach(System.out::println);
                                        log.info("{} API success, total: {}", getApiName(), restaurants.size());
                                        return ApiCallResult.success(getApiName(), restaurants);
                                    });
                            } else {
                                log.warn("{} API failed, status: {}", getApiName(), response.statusCode());
                                return Mono.just(ApiCallResult.failure(getApiName(), "HTTP " + response.statusCode().value()));
                            }
                        });
        })
        .timeout(Duration.ofSeconds(getTimeoutSeconds()))
        .doOnError(TimeoutException.class, e -> 
            log.error("{} API timeout", getApiName()))
        .doOnError(WebClientException.class, e -> 
            log.error("{} API Network error", getApiName(), e))
        .onErrorReturn(ApiCallResult.failure(getApiName(), "API call error"));
    }

    protected int getTimeoutSeconds() {
        return 5;
    }
    // @Override 
    // public final Mono<List<Restaurant>> search(RestaurantSearchRequest request) {
    //     String apiName = getApiName();
    //     log.info("Requesting search to {} API with query: {}", apiName, request.getQuery());

    //     // buildUri 로직을 추상 클래스로 이동, baseUrl 은 공통이므로 여기서 사용
    //     URI uri = buildUri(request, properties.getBaseUrl());

    //     return webClient.get()
    //         .uri(uri)
    //         .headers(this::addHeaders)
    //         .retrieve()
    //         .bodyToMono(getResponseDtoClass())
    //         .map(this::toRestaurants)
    //         .doOnSuccess(results -> log.info("Successfully received {} results from {} API", results.size(), apiName))
    //         .doOnError(e -> log.error("{} API request failed", apiName, e))
    //         .onErrorResume(e -> Mono.empty());
    // }

    // URI 생성 로직의 공통 부분을 부모로 올리고, 세부 경로와 파라미터만 자식에게 위임
    private URI buildUri(RestaurantSearchRequest request, String baseUrl) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl);
        // path 와 query param 설정은 각 API 마다 다르므로 자식 클래스에게 위임
        return buildUriDetails(builder, request).build().encode().toUri();
    }

    // 하위 클래스가 구현해야 할 추상 메소드
    protected abstract String getApiName();

    /*
     * URI path 와 query parameter 등 세부 정보를 설정합니다.
     */
    protected abstract UriComponentsBuilder buildUriDetails(UriComponentsBuilder builder, RestaurantSearchRequest request);

    protected abstract void addHeaders(HttpHeaders headers);

    protected abstract Class<T> getResponseDtoClass();

    protected abstract List<Restaurant> toRestaurants(T responseDto);
}
