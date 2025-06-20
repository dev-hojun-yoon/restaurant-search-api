package kr.hhplus.be.server.dto;

public class NaverRestaurantItem {
    private String title;
    private String category;
    private String address;
    private String mapx;
    private String mapy;

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getAddress() {
        return address;
    }

    public String getMapX() {
        return mapx;
    }

    public String getMapY() {
        return mapy;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMapx(String mapx) {
        this.mapx = mapx;
    }

    public void setMapy(String mapy) {
        this.mapy = mapy;
    }

    


}
