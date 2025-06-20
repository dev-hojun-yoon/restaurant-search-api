package kr.hhplus.be.server.dto;

public class KakaoRestaurantItem {
    private String title;
    private String category;
    private String address;
    private String x;
    private String y;
    
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
        return x;
    }
    public String getMapY() {
        return y;
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
    public void setX(String x) {
        this.x = x;
    }
    public void setY(String y) {
        this.y = y;
    }

       
}
