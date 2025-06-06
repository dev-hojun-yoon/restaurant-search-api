package kr.hhplus.be.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class NaverApiResponse {
    private List<NaverRestaurantItem> items;

    public List<NaverRestaurantItem> getItems() {
        return items;
    }
}
