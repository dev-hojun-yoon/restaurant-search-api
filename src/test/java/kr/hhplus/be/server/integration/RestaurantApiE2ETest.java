package kr.hhplus.be.server.integration;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;


import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
// import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.infrastructure.external.KakaoApiClient;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;
import kr.hhplus.be.server.infrastructure.external.RedisLockManager;
import kr.hhplus.be.server.infrastructure.persistence.restaurant.JpaRestaurantRepository;
import kr.hhplus.be.server.infrastructure.persistence.restaurant.RestaurantEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.test.web.servlet.MvcResult;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("Restaurant E2E test")
class RestaurantApiE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private JpaRestaurantRepository jpaRestaurantRepository;

    @Autowired
    private TestDbHelper testDbHelper;

    @Autowired
    private Environment env;

    @Autowired
    private NaverApiClient naverApiClient;
    
    @Autowired
    private KakaoApiClient kakaoApiClient;
    

    @PersistenceContext
    private EntityManager em;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public NaverApiClient naverApiClient() {
            return Mockito.mock(NaverApiClient.class);
        }

        @Bean
        @Primary
        public KakaoApiClient kakaoApiClient() {
            return Mockito.mock(KakaoApiClient.class);
        }
    }

    @BeforeEach
    void setUp() {
        testDbHelper.cleanUp();
    }

    @Test
    void testSaveAndQuery() {
        testDbHelper.createTestRestaurants();

        List<Restaurant> saved = restaurantRepository.findByQuery("불고기");
        log.info("저장된 데이터 확인: {}", saved.size());
        assertEquals(2, saved.size());        
    }


    @Test
    @DisplayName("외부 API 실패 시 DB에서 레스토랑 데이터를 성공적으로 조회")
    void shouldReturnRestaurantsFromDbWhenExternalFails() throws Exception {
        jpaRestaurantRepository.deleteAll();
        testDbHelper.createTestRestaurants();

        // 데이터가 실제로 저장되었는지 확인
        long count = jpaRestaurantRepository.count();
        log.info("저장된 레스토랑 개수: {}", count);

        // 각 테스트 메서드에서 모킹 동작 재설정
        Mockito.reset(naverApiClient, kakaoApiClient);

        // when(naverApiClient.search(any()))
        //     .thenThrow(new RuntimeException("네이버 API 실패"));

        // when(kakaoApiClient.search(any()))
        //     .thenThrow(new RuntimeException("카카오 API 실패"));
        when(naverApiClient.search(any()))
            .thenReturn(Mono.error(new RuntimeException("네이버 API 실패")));
        
        when(kakaoApiClient.search(any()))
            .thenReturn(Mono.error(new RuntimeException("카카오 API 실패")));

        // API 호출 및 응답 저장
        webTestClient.get()
            .uri(
                uriBuilder -> uriBuilder
                .path("api/v1/search/restaurants")
                .queryParam("query", "불고기")
                .queryParam("sort", "random")
                .build()
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.restaurants").isArray()
            .jsonPath("$.restaurants.length()").isEqualTo(2)

            .jsonPath("$.restaurants[0].title").isEqualTo("불고기 맛집")
            .jsonPath("$.restaurants[0].category").isEqualTo("한식")
            .jsonPath("$.restaurants[0].address").isEqualTo("서울 강남구")

            .jsonPath("$.restaurants[1].title").isEqualTo("불고기 전문점")
            .jsonPath("$.restaurants[1].category").isEqualTo("한식")
            .jsonPath("$.restaurants[1].address").isEqualTo("서울 서초구");
    }
}
