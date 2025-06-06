package kr.hhplus.be.server.dto;

public class RestaurantResponse {
    private String title;
    private String category;
    private String address;
    private String mapx;
    private String mapy;

    public RestaurantResponse(String title, String category, String address, String mapx, String mapy) {
        this.title = title;
        this.category = category;
        this.address = address;
        this.mapx = mapx;
        this.mapy = mapy;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public String getMapx() {
        return mapx;
    }

    public String getMapy() {
        return mapy;
    }
}
