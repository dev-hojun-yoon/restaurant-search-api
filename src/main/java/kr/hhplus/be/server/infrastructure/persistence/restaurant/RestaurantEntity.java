package kr.hhplus.be.server.infrastructure.persistence.restaurant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.restaurant.Restaurant;

@Entity
@Table(name = "restaurant")
public class RestaurantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String category;
    private String roadAddress;
    private double mapX;
    private double mapY;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public void setMapX(double mapX) {
        this.mapX = mapX;
    }

    public void setMapY(double mapY) {
        this.mapY = mapY;
    }

    // domain -> entity 변환
    public static RestaurantEntity fromDomain(Restaurant restaurant) {
        RestaurantEntity entity = new RestaurantEntity();
        entity.setTitle(restaurant.getTitle());
        entity.setCategory(restaurant.getCategory());
        entity.setRoadAddress(restaurant.getAddress());
        entity.setMapX(restaurant.getMapx());
        entity.setMapY(restaurant.getMapy());
        return entity;
    }

    // entity -> domain 변환
    public Restaurant toDomain() {
        return new Restaurant(
            this.title,
            this.category,
            this.roadAddress,
            Double.toString(this.mapX),
            Double.toString(this.mapY)
        );
    }
}
