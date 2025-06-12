package kr.hhplus.be.server.infrastructure.persistence.keyword;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "popular_keyword")
public class PopularKeywordEntity {
   
    @Id
    private String keyword;

    private long count;

    protected PopularKeywordEntity() {}
    
    public PopularKeywordEntity(String keyword) {
        this.keyword = keyword;
        this.count = 1;
    }

    public String getKeyword() {
        return keyword;
    }

    public long getCount() {
        return count;
    }

    public void increaseCount() {
        this.count++;
    }
}
