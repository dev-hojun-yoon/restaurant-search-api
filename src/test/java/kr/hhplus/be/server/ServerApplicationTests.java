package kr.hhplus.be.server;

import kr.hhplus.be.server.application.search.RestaurantService;
import kr.hhplus.be.server.application.search.RestaurantTransactionService;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.external.ApiCallResult;
import kr.hhplus.be.server.infrastructure.external.KakaoApiClient;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;
import kr.hhplus.be.server.kafka.KafkaProducerService;
import kr.hhplus.be.server.event.RestaurantSearchEvent;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher; // Keep this import for now, will remove if not needed after all changes
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;

// @SpringBootTest
@ExtendWith(MockitoExtension.class)
class ServerApplicationTests {
	// @Test
	// void contextLoads() {
		
	// }
	@Mock
	private NaverApiClient naverApiClient;

	@Mock
    private KakaoApiClient kakaoApiClient;

    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private PopularKeywordRepository keywordRepository;

    @Mock
    private RestaurantTransactionService transactionService;

    @Mock
    private KafkaProducerService kafkaProducerService; // Added KafkaProducerService mock

	@InjectMocks
	private RestaurantService restaurantService;

	@Test
	void shouldReturnResultsFromNaverApiClient() {
		// 임의 값 설정
		RestaurantSearchRequest request = new RestaurantSearchRequest();
		request.setQuery("강남 맛집");
		request.setSort("random");
		request.setOffset(1);
		request.setSize(10);
		request.setUserId("testUser");

		List<Restaurant> mockResponse = List.of(
				new Restaurant("맛집 1", "한식", "서울 강남구 대치동", "123", "456"),
				new Restaurant("맛집 2", "양식", "서울 강남구 논현동", "135", "495")
		);
		
		ApiCallResult mockResult = new ApiCallResult(mockResponse, true, null, "Naver");
		Mockito.when(naverApiClient.search(Mockito.any(RestaurantSearchRequest.class))).thenReturn(Mono.just(mockResult));
		// Mono<RestaurantResponse> result = restaurantService.searchRestaurants(request);

		// assertThat(result).isEqualTo(mockResponse);
		StepVerifier.create(restaurantService.searchRestaurants(request))
			.expectNextMatches(response -> {
				System.out.println("Response: " + response); // 디버깅용
				return response.getRestaurants().size() == 2 && response.isSuccess();
			})
			.verifyComplete();

		// Verify that KafkaProducerService.sendRestaurantSearchEvent was called
		Mockito.verify(kafkaProducerService, Mockito.times(1)).sendRestaurantSearchEvent(Mockito.any(RestaurantSearchEvent.class));
	}
}
