package kr.hhplus.be.server.domain.restaurant;

public class Restaurant {
    private String title;
    private String category;
    private String address;
    private int mapx;
    private int mapy;

    public Restaurant(String title, String category, String address, int mapx, int mapy) {
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

    public int getMapx() {
        return mapx;
    }

    public int getMapy() {
        return mapy;
    }
}
