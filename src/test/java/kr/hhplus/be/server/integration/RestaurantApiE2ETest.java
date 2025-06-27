package kr.hhplus.be.server.integration;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.infrastructure.persistence.restaurant.RestaurantEntity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Restaurant E2E test")
class RestaurantApiE2ETest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        System.out.println("MockMvc: " + mockMvc);
        restaurantRepository.save(
            List.of(
                new Restaurant("불고기 맛집", "한식", "서울 강남구", "123", "456")
            )
        );
    }

    @Test
    void shouldReturnRestaurantsFromDbWhenExternalFails() throws Exception {
        mockMvc.perform(
            get("/api/v1/search/restaurants")
            .contentType(MediaType.APPLICATION_JSON)
            .param("query", "불고기 맛집")
            .param("sort", "random")
        )
        .andExpect(status().isOk())
        .andDo(result -> System.out.println("테스트 결과 확인: " + result.getResponse().getContentAsString()))
        .andExpect(jsonPath("$.restaurants[0].title").value("불고기 맛집"));
    }
}
