package kr.hhplus.be.server.dto;

import lombok.Data;
import lombok.NonNull;

@Data // getter, setter, toString, equals, hashcode 등을 모두 포함
public class RestaurantSearchRequest {
    @NonNull
    private String query;

    private String sort = "random";
    private int offset = 1;
    private int size = 5;

    public RestaurantSearchRequest() {}

    public @NonNull String getQuery() {
        return query;
    }

    public String getSort() {
        return sort;
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }

    public void setQuery(@NonNull String query) {
        this.query = query;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
