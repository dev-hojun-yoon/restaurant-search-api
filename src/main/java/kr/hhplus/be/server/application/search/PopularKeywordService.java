package kr.hhplus.be.server.application.search;

import java.util.List;

import org.springframework.stereotype.Service;

import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PopularKeywordService {
    private final PopularKeywordRepository repository;

    public List<PopularKeyword> findTop(int limit) {
        return repository.findTopKeywords(limit);
    }
}
