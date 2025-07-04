package kr.hhplus.be.server.domain.keyword;

public class PopularKeyword {
    private final String keyword;
    private final long count;
    private final String region;

    // public PopularKeyword(String keyword, long count) {
    //     this.keyword = keyword;
    //     this.count = count;
    //     this.region = null; // 전체 지역 검색 시
    // }

    public PopularKeyword(String keyword, long count, String region) {
        this.keyword = keyword;
        this.count = count;
        this.region = region;
    }

    public String getKeyword() {
        return keyword;
    }

    public long getCount() {
        return count;
    }

    public String getRegion() {
        return region;
    }
}
