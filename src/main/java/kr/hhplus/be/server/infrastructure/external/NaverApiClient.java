package kr.hhplus.be.server.infrastructure.external;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.dto.NaverApiResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.config.NaverApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class NaverApiClient extends AbstractApiClient<NaverApiResponse, NaverApiProperties> {

    public NaverApiClient(WebClient webClient, NaverApiProperties properties) {
        super(webClient, properties);
    }

    @Override
    protected String getApiName() {
        return "Naver";
    }

    @Override
    protected UriComponentsBuilder buildUriDetails(UriComponentsBuilder builder, RestaurantSearchRequest request) {
        System.out.println(request.toString());
        return builder.path("/v1/search/local.json")
                .queryParam("query", request.getQuery())
                .queryParam("sort", request.getSort())
                .queryParam("start", (request.getOffset() - 1) * 5 + 1)
                .queryParam("display", 5); // 예시
    }

    @Override
    protected void addHeaders(HttpHeaders headers) {
        // this.properties 는 NaverApiProperties 타입이므로 고유 메서드 호출 가능
        headers.set("X-Naver-Client-Id", this.properties.getClientId());
        headers.set("X-Naver-Client-Secret", this.properties.getClientSecret());
    }

    @Override
    protected Class<NaverApiResponse> getResponseDtoClass() {
        return NaverApiResponse.class;
    }

    @Override
    protected List<Restaurant> toRestaurants(NaverApiResponse response) {
        if (response == null || response.getItems() == null) {
            return Collections.emptyList();
        }

        return response.getItems().stream()
                .filter(item -> item != null && item.getTitle() != null)
                .map(item -> new Restaurant(
                    cleanHtmlTags(item.getTitle()), 
                    item.getCategory(), 
                    item.getAddress(), 
                    item.getMapX(), 
                    item.getMapY()
                ))
                .collect(Collectors.toList());
    }

    private String cleanHtmlTags(String text) {
        if (text == null) return null;
        return text.replaceAll("<[^>]*>", "");
    }
}

// @Slf4j
// @Component("naverApiClient")
// @RequiredArgsConstructor
// public class NaverApiClient implements ApiClient {
//     private final WebClient webClient;
//     private final NaverApiProperties properties;

//     @Override
//     public Mono<List<Restaurant>> search(RestaurantSearchRequest request) {
//         log.info("Requesting search to Naver API with query: {}", request.getQuery());
//         UriComponentsBuilder uriBuilder = UriComponentsBuilder
//                 .fromUriString("https://openapi.naver.com")
//                 .path("/v1/search/local.json")
//                 .queryParam("query", request.getQuery())
//                 .queryParam("display", request.getSize())
//                 .queryParam("start", request.getOffset());

//         if (request.getSort() != null && !Objects.equals(request.getSort(), "random")) {
//             uriBuilder.queryParam("sort", request.getSort());
//         }

//         URI uri = uriBuilder.build().encode().toUri();
//         System.out.println("Naver uri: " + uri);

//         return webClient.get()
//             .uri(uri)
//             .header("X-Naver-Client-Id", properties.getClientId())
//             .header("X-Naver-Client-Secret", properties.getClientSecret())
//             .retrieve()
//             .bodyToMono(NaverApiResponse.class)
//             .map(this::toRestaurants)
//             .doOnSuccess(results -> log.info("Successfully received {} results from Naver API", results.size()))
//             .doOnError(e -> log.error("Naver API request failed", e))
//             .onErrorResume(e -> Mono.empty());
//     }

//     private List<Restaurant> toRestaurants(NaverApiResponse response) {
//         // Naver API 응답을 표준 Restaurant DTO 리스트로 변환
//         return response.getItems().stream()
//             .map(item -> new Restaurant(item.getTitle(), item.getCategory(), item.getAddress(), item.getMapx(), item.getMapy()))
//             .collect(Collectors.toList());
//     }
// }
