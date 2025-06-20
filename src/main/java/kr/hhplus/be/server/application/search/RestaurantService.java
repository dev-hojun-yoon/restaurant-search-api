package kr.hhplus.be.server.application.search;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.external.ApiCallResult;
import kr.hhplus.be.server.infrastructure.external.KakaoApiClient;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {
    
    private final NaverApiClient naverApiClient;
    private final KakaoApiClient kakaoApiClient; // KakaoApiClient 의존성 추가
    private final RestaurantRepository repository;
    private final PopularKeywordService keywordService;

    // 블로킹 I/O (DB 접근)을 위한 별도의 스케줄러
    private final Scheduler jdbcScheduler = Schedulers.boundedElastic();

    private final Scheduler apiScheduler = Schedulers.parallel();

    /*
     * 맛집 검색하고, 검색 키워드 및 결과를 DB에 저장한다.
     * 1. 네이버 API 를 호출한다.
     * 2. 결과가 비어있으면 (혹은 에러가 확인되면), 카카오 API 를 호출한다.
     * 3. 최종 결과를 DB에 저장하고, 검색 키워드 카운트를 증가시킨다.
     * 4. 외부 API 둘다 실패하면 DB 에서 검색
     */

    public Mono<RestaurantResponse> searchRestaurants(RestaurantSearchRequest request) {
        log.info("Restaurant search start!: {}", request.getQuery());

        // 입력 유효성 검증
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return Mono.just(RestaurantResponse.failure("검색어를 입력해주세요."));
        }

        // 외부 API 동시 호출
        return callExternalApis(request)
            .flatMap(result -> {
                if (result.hasResults()) {
                    // 외부 API 성공 시 DB 저장 및 키워드 기록
                    return saveResultsAndUpdateKeyword(result.getRestaurants(), request.getQuery())
                            .thenReturn(RestaurantResponse.success(result.getRestaurants(), result.getApiName()));
                } else {
                    log.info("외부 API 모두 실패, DB 검색 시도");
                    // DB 검색
                    return searchFromDatabase(request)
                            .flatMap(dbResults -> {
                                return increaseKeywordCount(request.getQuery())
                                    .thenReturn(dbResults.isEmpty() ? 
                                        RestaurantResponse.failure("검색 결과를 찾을 수 없습니다.") :
                                        RestaurantResponse.success(dbResults, "DATABASE"));
                            });
                }
            })
            .doOnSuccess(response -> log.info("검색 완료: {} ({})", response.getMessage(), response.getDataSource()))
            .doOnError(error -> log.error("검색 중 오류 발생", error))
            .onErrorReturn(RestaurantResponse.failure("서비스 중 오류가 발생했습니다."));
    }

    private Mono<ApiCallResult> callExternalApis(RestaurantSearchRequest request) {
        Mono<ApiCallResult> naverCall = naverApiClient.search(request)
            .subscribeOn(apiScheduler);

        Mono<ApiCallResult> kakaoCall = kakaoApiClient.search(request)
            .subscribeOn(apiScheduler);

        // 두 API 를 동시에 호출하고 첫번째 성공 결과를 반환
        return Mono.firstWithValue(
            naverCall.filter(ApiCallResult::hasResults),
            kakaoCall.filter(ApiCallResult::hasResults)
        )
        .switchIfEmpty(
            // 둘다 결과가 없으면 둘 중 하나라도 성공한 것을 반환 (빈 결과라도)
            Mono.firstWithValue(
                naverCall.filter(ApiCallResult::isSuccess),
                kakaoCall.filter(ApiCallResult::isSuccess)
            )
        )
        .switchIfEmpty(
            // 둘다 실패한 경우
            Mono.zip(naverCall, kakaoCall)
                .map(tuple -> {
                    ApiCallResult naver = tuple.getT1();
                    ApiCallResult kakao = tuple.getT2();
                    log.warn("모든 외부 API 실패 // Naver: {}, Kakao: {}", naver.getErrorMessage(), kakao.getErrorMessage());
                    return ApiCallResult.failure("ALL APIS", "모든 외부 API 호출 실패");
                })
        );

    }

    // DB 에서 검색
    private Mono<List<Restaurant>> searchFromDatabase(RestaurantSearchRequest request) {
        return Mono.fromCallable(() -> {
            try {
                log.info("DB에서 검색 시작: {}", request.getQuery());
                List<Restaurant> results = repository.findByQuery(request.getQuery());
                log.info("result total: {}", results.size());
                return results; // TODO: 응답 객체 활용
            } catch (Exception e) {
                log.error("DB 검색 중 오류", e);
                return Collections.<Restaurant>emptyList();
            }
        })
        .subscribeOn(jdbcScheduler);
    }

    // 검색 결과 저장 및 키워드 카운트 업데이트
    private Mono<Void> saveResultsAndUpdateKeyword(List<Restaurant> restaurants, String query) {
        return Mono.fromRunnable(() -> {
            try {
                log.info("DB에 {} 개 결과 저장 및 키워드 카운트 업데이트", restaurants.size());
                repository.save(restaurants);
                keywordService.increaseCount(query);
                log.info("DB 저장 완료");
            } catch (Exception e) {
                log.error("DB 저장 중 오류 발생", e);
            }
        })
        .subscribeOn(jdbcScheduler)
        .then();
    }

    // 키워드 카운트만 증가 (DB 검색 시 사용)
    private Mono<Void> increaseKeywordCount(String query) {
        return Mono.fromRunnable(() -> {
            try {
                keywordService.increaseCount(query);
                log.info("키워드 카운트 증가 완료: {}", query);
            } catch (Exception e) {
                log.error("키워드 카운트 증가 중 오류 발생", e);
            }
        })
        .subscribeOn(jdbcScheduler)
        .then();
    }
    

    // public Mono<List<Restaurant>> searchRestaurants(RestaurantSearchRequest request) {
    //     // 네이버 API 호출
    //     return naverApiClient.search(request)
    //             .filter(list -> !list.isEmpty())
    //             .switchIfEmpty(kakaoApiClient.search(request))
    //             .flatMap(restaurantList -> {
    //                 if (restaurantList.isEmpty()) {
    //                     log.info("No results found from any API for query: {}", request.getQuery());
    //                     return Mono.just(restaurantList);
    //                 }

    //                 // DB 작업은 블로킹이므로 별도 스레드에서 진행
    //                 return Mono.fromRunnable(() -> {
    //                         log.info("Saving {} results to DB and updating keyword count.", restaurantList.size());
    //                         repository.save(restaurantList);
    //                         keywordRepository.increaseCount(request.getQuery());
    //                     })
    //                     .subscribeOn(jdbcScheduler)
    //                     .thenReturn(restaurantList);
                    
    //             });
    // }

    // public List<Restaurant> searchRestaurants(RestaurantSearchRequest request) {
    //     List<Restaurant> externalApiResponse = naverApiClient.search(request);
    //     repository.save(externalApiResponse); // 검색 기록 저장
    //     keywordRepository.increaseCount(request.getQuery()); // 인기 키워드 카운트

    //     return externalApiResponse;
    // }
}
