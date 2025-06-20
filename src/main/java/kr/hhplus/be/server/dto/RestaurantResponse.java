package kr.hhplus.be.server.dto;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
    private List<Restaurant> restaurants;
    private String dataSource; // "Naver", "Kakao", "Database", "Cache"
    private boolean success;
    private String message;

    public static RestaurantResponse success(List<Restaurant> restaurants, String dataSource) {
        String message = restaurants.isEmpty() ? 
            "Results not found." : 
            String.format("(%s) Total: %d ", dataSource, restaurants.size());
        return new RestaurantResponse(restaurants, dataSource, true, message);
    }

    public static RestaurantResponse failure(String message) {
        return new RestaurantResponse(Collections.emptyList(), null, false, message);
    }

    public Mono<ResponseEntity<List<RestaurantResponse>>> stream() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stream'");
    }
}

// public class RestaurantResponse {
//     private String title;
//     private String category;
//     private String address;
//     private float mapx;
//     private float mapy;

//     public RestaurantResponse(Restaurant restaurant) {
//         this.title = restaurant.getTitle();
//         this.category = restaurant.getCategory();
//         this.address = restaurant.getAddress();
//         this.mapx = restaurant.getMapx();
//         this.mapy = restaurant.getMapy();
//     }
// }
