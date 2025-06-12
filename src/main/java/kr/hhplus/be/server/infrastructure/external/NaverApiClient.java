package kr.hhplus.be.server.infrastructure.external;
import kr.hhplus.be.server.config.NaverApiProperties;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.dto.NaverApiResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Component
public class NaverApiClient {
    private final WebClient webClient;
    private final NaverApiProperties properties;

    public NaverApiClient (WebClient webClient, NaverApiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public List<Restaurant> search(RestaurantSearchRequest request) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/local.json")
                .queryParam("query", request.getQuery())
                .queryParam("display", request.getSize())
                .queryParam("start", request.getOffset());

        if (request.getSort() != null && !Objects.equals(request.getSort(), "random")) {
            uriBuilder.queryParam("sort", request.getSort());
        }

        URI uri = uriBuilder.build().encode().toUri();
        System.out.println("uri: " + uri);

        NaverApiResponse response = webClient.get()
                .uri(uri)
                .header("X-Naver-Client-Id", properties.getClientId())
                .header("X-Naver-Client-Secret", properties.getClientSecret())
                .retrieve()
                .bodyToMono(NaverApiResponse.class)
                .block();

        if (response == null || response.getItems() == null) {
            return List.of();
        }

        return response.getItems().stream()
                .map(item -> new Restaurant(
                        item.getTitle(),
                        item.getCategory(),
                        item.getAddress(),
                        item.getMapx(),
                        item.getMapy()))
                .toList();

//        NaverApiResponse response = webClient.get()
//                .uri(urlBuilder -> urlBuilder
//                        .path("/v1/search/local.json")
//                        .queryParam("query", request.getQuery())
//                        .queryParam("display", request.getSize())
//                        .queryParam("start", request.getOffset())
//                        .queryParam("sort", request.getSort())
//                        .build())
//                .header("X-Naver-Client-id", properties.getClientId())
//                .header("X-Naver-Client-Secret", properties.getClientSecret())
//                .retrieve()
//                .bodyToMono(NaverApiResponse.class)
//                .block();
//
//        return response.getItems().stream()
//                .map(item -> new RestaurantResponse(
//                        item.getTitle(),
//                        item.getCategory(),
//                        item.getAddress(),
//                        item.getMapx(),
//                        item.getMapy()))
//                .toList();
    }
}
