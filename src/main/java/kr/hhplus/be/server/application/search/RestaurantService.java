package kr.hhplus.be.server.application.search;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.keyword.PopularKeywordRepository;
import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.infrastructure.external.ApiCallResult;
import kr.hhplus.be.server.infrastructure.external.KakaoApiClient;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;
import kr.hhplus.be.server.infrastructure.external.RedisLockManager;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {
    
    private final NaverApiClient naverApiClient;
    private final KakaoApiClient kakaoApiClient; // KakaoApiClient 의존성 추가
    private final RestaurantRepository repository;
    private final PopularKeywordRepository keywordRepository;
    private final RestaurantTransactionService transactionService;
    private final RedisLockManager redisLockManager;

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

    // public Mono<RestaurantResponse> searchRestaurants(RestaurantSearchRequest request) {
    //     log.info("Restaurant search start!: {}", request.getQuery());

    //     // 입력 유효성 검증
    //     if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
    //         return Mono.just(RestaurantResponse.failure("검색어를 입력해주세요."));
    //     }

    //     // 외부 API 동시 호출
    //     return callExternalApis(request)
    //         .flatMap(result -> {
    //             if (result.hasResults()) {
    //                 // 외부 API 성공 시 DB 저장 및 키워드 기록
    //                 return saveResultsAndUpdateKeyword(result.getRestaurants(), request.getQuery())
    //                         .thenReturn(RestaurantResponse.success(result.getRestaurants(), result.getApiName()));
    //             } else {
    //                 log.info("외부 API 모두 실패, DB 검색 시도");
    //                 // DB 검색
    //                 return searchFromDatabase(request)
    //                         .flatMap(dbResults -> {
    //                             return increaseKeywordCount(request.getQuery())
    //                                 .thenReturn(dbResults.isEmpty() ? 
    //                                     RestaurantResponse.failure("검색 결과를 찾을 수 없습니다.") :
    //                                     RestaurantResponse.success(dbResults, "DATABASE"));
    //                         });
    //             }
    //         })
    //         .doOnSuccess(response -> log.info("검색 완료: {} ({})", response.getMessage(), response.getDataSource()))
    //         .doOnError(error -> log.error("검색 중 오류 발생", error))
    //         .onErrorReturn(RestaurantResponse.failure("서비스 중 오류가 발생했습니다."));
    // }

    public Mono<RestaurantResponse> searchRestaurants(RestaurantSearchRequest request) {
        String lockKey = "lock:search" + request.getQuery();
        String lockValue = UUID.randomUUID().toString();

        boolean isLockAcquired = redisLockManager.lock(lockKey, lockValue);
        if (!isLockAcquired) {
            throw new IllegalStateException("동일한 검색이 이미 진행 중이다. 잠시 후 시도해주세요.");
        }

        try {
            return validateRequest(request)
                .flatMap(this::searchWithFallback)
                .flatMap(tuple -> processSearchResult(tuple.getT1(), tuple.getT2(), request.getQuery()))
                .doOnSuccess(response -> log.info("검색 완료: {}", response.getMessage()))
                .doOnError(e -> log.error("에러 발생", e))
                .onErrorReturn(RestaurantResponse.failure("서비스 중 오류가 발생했습니다."));
        } finally {
            redisLockManager.unlock(lockKey, lockValue);
        }
        
    }

    private Mono<RestaurantSearchRequest> validateRequest(RestaurantSearchRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("검색어를 입력해주세요."));
        }

        return Mono.just(request);
    }

    private Mono<Tuple2<List<Restaurant>, String>> searchWithFallback(RestaurantSearchRequest request) {
        return callExternalApis(request)
                .flatMap(result -> {
                    if (result.hasResults()) {
                        return Mono.just(Tuples.of(result.getRestaurants(), result.getApiName()));
                    } else {
                        log.info("외부 API 모두 실패, DB 검색 시도");
                        return searchFromDatabase(request)
                                .map(dbResults -> Tuples.of(dbResults, "DATABASE"));
                    }
                });
    }

    private Mono<RestaurantResponse> processSearchResult(List<Restaurant> restaurants, String dataSource, String query) {
        if (restaurants == null || restaurants.isEmpty()) {
            return Mono.just(RestaurantResponse.failure("검색 결과를 찾을 수 없습니다."));
        }

        Mono<Void> writeTask = "DATABASE".equals(dataSource)
            ? increaseKeywordCount(query)
            : saveResultsAndUpdateKeyword(restaurants, query);

        return writeTask.thenReturn(RestaurantResponse.success(restaurants, dataSource));
    }

    private Mono<ApiCallResult> callExternalApis(RestaurantSearchRequest request) {
        // 네이버 -> 카카오 순서로 API 가 호출되도록 수정
        return naverApiClient.search(request)
                .subscribeOn(apiScheduler)
                .onErrorResume(naverError -> {
                    log.warn("naver api 호출 실패, kakao fallback 시도", naverError);
                    return kakaoApiClient.search(request)
                            .onErrorResume(kakaoError -> {
                                log.error("kakao api 호출도 실패", kakaoError);
                                return Mono.just(ApiCallResult.failure("ALL API", "naver/kakao api 모두 실패"));
                            });
                })
                .flatMap(naverResult -> {
                    if (naverResult.hasResults()) {
                        return Mono.just(naverResult);
                    } else if (naverResult.isSuccess()) {
                        return kakaoApiClient.search(request)
                                .subscribeOn(apiScheduler)
                                .onErrorResume(kakaoError -> {
                                    log.error("kakao api 호출 실패", kakaoError);
                                    return Mono.just(naverResult);
                                })
                                .map(kakaoResult -> kakaoResult.hasResults() ? kakaoResult : naverResult);
                    } else {
                        return Mono.just(ApiCallResult.failure("naver", "naver api 실패"));
                    }
                });
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
    public Mono<Void> saveResultsAndUpdateKeyword(List<Restaurant> restaurants, String query) {
        return Mono.fromRunnable(() -> transactionService.saveResultsAndUpdateKeywordBlocking(restaurants, query))
                .subscribeOn(jdbcScheduler)
                .then();
    }

    // 키워드 카운트만 증가 (DB 검색 시 사용)
    private Mono<Void> increaseKeywordCount(String query) {
        return Mono.fromRunnable(() -> {
            try {
                // keywordService.increaseCount(query);
                String region = keywordRepository.extractRegionFromKeyword(query);
                keywordRepository.increaseCount(query, region);
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
