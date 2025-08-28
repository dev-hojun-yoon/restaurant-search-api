package kr.hhplus.be.server.infrastructure.persistence.keyword;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popular_keyword")
@NoArgsConstructor
public class PopularKeywordEntity {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private long count;

    @Column(nullable = true)
    private String region;
    
    public PopularKeywordEntity(String keyword, String region) {
        this.keyword = keyword;
        this.region = region;
        this.count = 1;
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

    // public void increaseCount() {
    //     this.count++;
    // }

    public void increment(long delta) {
        this.count += delta;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    
}
