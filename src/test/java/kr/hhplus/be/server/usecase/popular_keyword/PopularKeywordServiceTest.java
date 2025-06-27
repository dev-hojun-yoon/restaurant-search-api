package kr.hhplus.be.server.usecase.popular_keyword;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.application.search.PopularKeywordService;
import kr.hhplus.be.server.domain.keyword.PopularKeyword;
import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;

@ExtendWith(MockitoExtension.class)
public class PopularKeywordServiceTest {
    
    @Mock
    private PopularKeywordRepository keywordRepository;

    @InjectMocks
    private PopularKeywordService keywordService;

    @Test
    public void shouldReturnTop10Keywords() {
        List<PopularKeyword> mockKeywords = List.of(
            new PopularKeyword("강남 맛집", 20, "강남"),
            new PopularKeyword("한식", 5, null),
            new PopularKeyword("양식", 3, null)
        );

        Mockito.when(keywordRepository.findTopKeywords(10)).thenReturn(mockKeywords);
        List<PopularKeyword> result = keywordService.findTop(10);
        
        assertThat(result)
            .hasSize(3)
            .extracting(PopularKeyword::getKeyword, PopularKeyword::getCount, PopularKeyword::getRegion)
            .containsExactly(
                tuple("강남 맛집", (long) 20, "강남"),
                tuple("한식", (long) 5, null),
                tuple("양식", (long) 3, null)
            );
    }

    @Test
    public void shouldReturnTopKeywordsByRegion() {
        List<PopularKeyword> mockKeywords = List.of(
            new PopularKeyword("강남 맛집", 20, "강남"),
            new PopularKeyword("한식", 5, null),
            new PopularKeyword("양식", 3, null)
        );

        Mockito.when(keywordRepository.findTopKeywordsByRegion(10, "강남"))
            .thenReturn(List.of(mockKeywords.get(0))); // 강남 지역 키워드만 반환
        
        List<PopularKeyword> result = keywordService.findTopByRegion(10, "강남");

        assertThat(result)
            .hasSize(1)
            .extracting(PopularKeyword::getKeyword, PopularKeyword::getCount, PopularKeyword::getRegion)
            .containsExactly(
                tuple("강남 맛집", (long) 20, "강남")
            );
    }
}
