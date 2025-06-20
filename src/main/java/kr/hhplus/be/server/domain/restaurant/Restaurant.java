package kr.hhplus.be.server.domain.restaurant;

public class Restaurant {
    private String title;
    private String category;
    private String address;
    private String mapx;
    private String mapy;

    public Restaurant(String title, String category, String address, String mapx, String mapy) {
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

    public double getMapx() {
        return Double.parseDouble(mapx);
    }

    public double getMapy() {
        return Double.parseDouble(mapy);
    }

    @Override
    public String toString() {
        return "Restaurant [title=" + title + ", category=" + category + ", address=" + address + ", mapx=" + mapx
                + ", mapy=" + mapy + "]";
    }

    
}
