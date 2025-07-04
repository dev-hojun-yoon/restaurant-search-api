package kr.hhplus.be.server.infrastructure.persistence.restaurant;

import org.springframework.stereotype.Indexed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "restaurant")
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "mapx")
    private double mapX;

    @Column(name = "mapy")
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

    public RestaurantEntity updateFromDomain(Restaurant domain) {
        this.title = domain.getTitle();
        this.category = domain.getCategory();
        this.mapX = domain.getMapx();
        this.mapY = domain.getMapy();
        return this;
    }
}
