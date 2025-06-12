package kr.hhplus.be.server.domain.keyword;

import java.util.List;

public interface PopularKeywordRepository {
    void increaseCount(String keyword);
    List<PopularKeyword> findTopKeywords(int limit);
}
