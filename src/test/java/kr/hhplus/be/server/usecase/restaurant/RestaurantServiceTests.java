package kr.hhplus.be.server.usecase.restaurant;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import kr.hhplus.be.server.application.search.PopularKeywordService;
import kr.hhplus.be.server.application.search.RestaurantService;
import kr.hhplus.be.server.application.search.RestaurantTransactionService;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.config.NaverApiProperties;
import kr.hhplus.be.server.infrastructure.external.ApiCallResult;
import kr.hhplus.be.server.infrastructure.external.KakaoApiClient;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;
import kr.hhplus.be.server.infrastructure.external.RedisLockManager;
import kr.hhplus.be.server.infrastructure.persistence.restaurant.RestaurantEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.withSettings;


@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTests {
	@Mock
	private NaverApiClient naverApiClient;

	@Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private PopularKeywordService popularKeywordService;

    @Mock
    private RestaurantTransactionService transactionService;

    @Mock
    private RedisLockManager redisLockManager;

    @Mock
    private kr.hhplus.be.server.kafka.KafkaProducerService kafkaProducerService;

	@InjectMocks
	private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        Mockito.when(redisLockManager.acquireLock(anyString(), any())).thenReturn(Mono.just("lockValue"));
        Mockito.when(redisLockManager.releaseLock(anyString(), anyString())).thenReturn(Mono.empty());

        Mockito.when(popularKeywordService.extractRegionFromKeyword(anyString())).thenReturn("강남");
        Mockito.doNothing().when(popularKeywordService).increaseCount(anyString(), anyString());
        
        // void 메서드이므로 doNothing() 사용
        Mockito.doNothing().when(transactionService).saveResultsAndUpdateKeywordBlocking(anyList(), anyString());
        Mockito.doNothing().when(kafkaProducerService).sendRestaurantSearchEvent(any());
    }

    @Test
    @DisplayName("Naver_응답_성공")
    public void whenNaverSucceeds_thenReturnNaverResults() {
        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setQuery("강남 맛집");

        List<Restaurant> mockData = List.of(
            new Restaurant("맛집", "한식", "주소", "123", "456")
        );
        ApiCallResult result = new ApiCallResult(mockData, true, null, "Naver");
        Mockito.when(naverApiClient.search(request)).thenReturn(Mono.just(result));
        
        StepVerifier.create(restaurantService.searchRestaurants(request))
            .expectNextMatches(response -> response.getRestaurants().size() == 1)
            .verifyComplete();
    }

    @Test
    @DisplayName("Naver_실패_Kakao_성공")
    public void whenNaverFails_thenFallbackToKakao() {
        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setQuery("강남 맛집");

        Mockito.when(naverApiClient.search(request)).thenReturn(Mono.error(new RuntimeException("Naver Error")));
        List<Restaurant> kakaoData = List.of(
            new Restaurant("맛집2", "중식", "주소", "789", "000")
        );
        ApiCallResult kakaoResult = new ApiCallResult(kakaoData, true, null, "Kakao");
        Mockito.when(kakaoApiClient.search(request)).thenReturn(Mono.just(kakaoResult));

        StepVerifier.create(restaurantService.searchRestaurants(request))
            .expectNextMatches(response -> response.getRestaurants().get(0).getTitle().equals("맛집2"))
            .verifyComplete();
    }

    @Test
    @DisplayName("외부_API_실패_DB_성공")
    public void whenAllExternalFail_thenUseDatabase() {
        RestaurantSearchRequest request =  new RestaurantSearchRequest();
        request.setQuery("강남 맛집");

        Mockito.when(naverApiClient.search(request)).thenReturn(Mono.error(new RuntimeException("Naver Error")));
        Mockito.when(kakaoApiClient.search(request)).thenReturn(Mono.error(new RuntimeException("Kakao Error")));
        List<RestaurantEntity> dbResults = List.of(
            new RestaurantEntity((long) 123, "롯데리아", "패스트푸드", "서울 신정동", 123, 123)
        );
        List<Restaurant> dbResults_toRestaurant = dbResults.stream()
                                                .map(RestaurantEntity::toDomain)
                                                .collect(Collectors.toList());
        Mockito.when(restaurantRepository.findByQuery(anyString())).thenReturn(dbResults_toRestaurant);

        StepVerifier.create(restaurantService.searchRestaurants(request))
                .expectNextMatches(response -> response.getRestaurants().get(0).getTitle().equals("롯데리아"))
                .verifyComplete();
    }
}