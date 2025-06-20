package kr.hhplus.be.server;

import kr.hhplus.be.server.application.search.RestaurantService;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.external.ApiCallResult;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;
import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;

//@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ServerApplicationTests {

//	@TestConfiguration
//	static class TestConfig {
//		@Bean
//		@Primary
//		public NaverApiClient naverApiClient() {
//			return Mockito.mock(NaverApiClient.class);
//		}
//	}

//	@Autowired
//	private NaverApiClient naverApiClient;
//
//	@Autowired
//	private RestaurantService restaurantService;

	@Mock
	private NaverApiClient naverApiClient;

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

		List<Restaurant> mockResponse = List.of(
				new Restaurant("맛집 1", "한식", "서울 강남구 대치동", "123", "456"),
				new Restaurant("맛집 2", "양식", "서울 강남구 논현동", "135", "495")
		);
		
		ApiCallResult mockResult = new ApiCallResult(mockResponse, true, null, "Naver");
		Mockito.when(naverApiClient.search(request)).thenReturn(Mono.just(mockResult));
		Mono<RestaurantResponse> result = restaurantService.searchRestaurants(request);

		assertThat(result).isEqualTo(mockResponse);
		// assertThat(result).hasSize(2);
//		assertThat(result.get(0)).isEqualTo(mockResponse.get(0));


	}

}
