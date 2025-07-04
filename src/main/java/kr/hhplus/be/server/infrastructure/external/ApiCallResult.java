package kr.hhplus.be.server.infrastructure.external;

import java.util.Collections;
import java.util.List;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiCallResult {
    private List<Restaurant> restaurants;
    private boolean success;
    private String errorMessage;
    private String apiName;

    public static ApiCallResult success(String apiName, List<Restaurant> restaurants) {
        return new ApiCallResult(restaurants, true, null, apiName);
    }

    public static ApiCallResult failure(String apiName, String errorMessage) {
        return new ApiCallResult(Collections.emptyList(), false, errorMessage, apiName);
    }

    public boolean hasResults() {
        return success && restaurants != null && !restaurants.isEmpty();
    }

    
}
