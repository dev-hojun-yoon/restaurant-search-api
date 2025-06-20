package kr.hhplus.be.server.infrastructure.external;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.dto.KakaoApiResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.config.KakaoApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
public class KakaoApiClient extends AbstractApiClient<KakaoApiResponse, KakaoApiProperties> {

    public KakaoApiClient(WebClient webClient, KakaoApiProperties properties) {
        super(webClient, properties);
    }

    @Override
    protected String getApiName() {
        return "Kakao";
    }

    @Override
    protected UriComponentsBuilder buildUriDetails(UriComponentsBuilder builder, RestaurantSearchRequest request) {
        return builder.path("/v2/local/search/keyword.json")
                .queryParam("query", request.getQuery())
                .queryParam("category_group_code", "FD6")
                .queryParam("size", 5); // 예시
    }

    @Override
    protected void addHeaders(HttpHeaders header) {
        header.set("Authorization", "KakaoAK " + this.properties.getRestApiKey());
    }

    @Override
    protected Class<KakaoApiResponse> getResponseDtoClass() {
        return KakaoApiResponse.class;
    }

    @Override
    protected List<Restaurant> toRestaurants(KakaoApiResponse response) {
        return response.getItems().stream()
                .filter(item -> item != null && item.getTitle() != null)
                .map(item -> new Restaurant(
                    cleanHtmlTags(item.getTitle()),
                    item.getCategory(), 
                    item.getAddress(), 
                    item.getMapX(), 
                    item.getMapY())
                )
                .collect(Collectors.toList());
    }

    private String cleanHtmlTags(String text) {
        if (text == null) return null;
        return text.replaceAll("<[^>]*>", "");
    }
}

// @Slf4j
// @Component("kakaoApiClient")
// @RequiredArgsConstructor
// public class KakaoApiClient implements ApiClient{
//     private final WebClient webClient;
//     private final KakaoApiProperties properties;

//     @Override
//     public Mono<List<Restaurant>> search(RestaurantSearchRequest request) {
//         log.info("Requesting search to kakao API with query: {}", request.getQuery());
//         int page = request.getOffset() / request.getSize();
//         UriComponentsBuilder uriBuilder = UriComponentsBuilder
//             .fromUriString("https://dapi.kakao.com")
//             .path("/v2/local/search/keyword.json")
//             .queryParam("query", request.getQuery())
//             .queryParam("size", request.getSize())
//             .queryParam("page", page);

//         if (request.getSort() != null && !Objects.equals(request.getSort(), "accuracy")) {
//             uriBuilder.queryParam("sort", request.getSort());
//         }

//         URI uri = uriBuilder.build().encode().toUri();
//         System.out.println("Kakao uri: "+ uri);

//         return webClient.get()
//                 .uri(uri)
//                 .header("Authorization: KakaoAK ", properties.getRestApiKey())
//                 .retrieve()
//                 .bodyToMono(KakaoApiResponse.class)
//                 .map(this::)
//     }


// }
