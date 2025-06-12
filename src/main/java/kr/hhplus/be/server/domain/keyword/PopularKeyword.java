package kr.hhplus.be.server.domain.keyword;

public class PopularKeyword {
    private final String keyword;
    private final long count;

    public PopularKeyword(String keyword, long count) {
        this.keyword = keyword;
        this.count = count;
    }

    public String getKeyword() {
        return keyword;
    }

    public long getCount() {
        return count;
    }
    
}
