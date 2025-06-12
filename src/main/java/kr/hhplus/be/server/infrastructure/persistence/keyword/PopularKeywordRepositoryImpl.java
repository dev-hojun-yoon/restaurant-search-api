package kr.hhplus.be.server.infrastructure.persistence.keyword;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;

@Repository
public class PopularKeywordRepositoryImpl implements PopularKeywordRepository{
    
    private final JpaPopularKeywordRepository jpaRepository;

    public PopularKeywordRepositoryImpl(JpaPopularKeywordRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void increaseCount(String keyword) {
        PopularKeywordEntity entity = jpaRepository.findByKeyword(keyword)
                                .map(e -> {
                                    e.increaseCount();
                                    return e;
                                })
                                .orElse(new PopularKeywordEntity(keyword));
        jpaRepository.save(entity);
            
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'increaseCount'");
    }

    @Override
    public List<PopularKeyword> findTopKeywords(int limit) {
        return jpaRepository.findAllByOrderByCountDesc(PageRequest.of(0 , limit)).stream()
                .map(e -> new PopularKeyword(e.getKeyword(), e.getCount()))
                .toList();
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'findTopKeywords'");
    }


    
}
