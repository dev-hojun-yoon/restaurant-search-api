package kr.hhplus.be.server.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoApiResponse {
    private List<KakaoRestaurantItem> items;
    
    public List<KakaoRestaurantItem> getItems() {
        return items;
    }
}
