package kr.hhplus.be.server.application.search;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

import kr.hhplus.be.server.domain.restaurant.Restaurant;
import kr.hhplus.be.server.domain.restaurant.RestaurantRepository;
import kr.hhplus.be.server.dto.RestaurantResponse;
import kr.hhplus.be.server.dto.RestaurantSearchRequest;
import kr.hhplus.be.server.kafka.KafkaProducerService;
import kr.hhplus.be.server.event.RestaurantSearchEvent;
import kr.hhplus.be.server.infrastructure.external.ApiCallResult;
import kr.hhplus.be.server.infrastructure.external.KakaoApiClient;
import kr.hhplus.be.server.infrastructure.external.NaverApiClient;
import kr.hhplus.be.server.infrastructure.external.RedisLockManager;
import lombok.RequiredArgsConstructor;
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
    private final KakaoApiClient kakaoApiClient;
    private final RestaurantRepository repository;
    private final PopularKeywordService popularKeywordService; // 변경
    private final RestaurantTransactionService transactionService;
    private final RedisLockManager redisLockManager;
    private final KafkaProducerService kafkaProducerService;

    private final Scheduler jdbcScheduler = Schedulers.boundedElastic();
    private final Scheduler apiScheduler = Schedulers.parallel();

    public Mono<RestaurantResponse> searchRestaurants(RestaurantSearchRequest request) {
        String lockKey = "lock:search:" + request.getQuery() + (request.getUserId() != null ? ":" + request.getUserId() : "");

        return redisLockManager.acquireLock(lockKey, Duration.ofSeconds(5))
                .flatMap(acquiredValue -> {
                    if (acquiredValue == null) {
                        log.warn("동일한 검색이 이미 진행 중입니다. DB에서 조회한 내용으로 대체합니다.");
                        return searchFromDatabase(request)
                                .flatMap(dbResults -> processSearchResult(dbResults, "DATABASE", request.getQuery(), request.getUserId()))
                                .doOnSuccess(response -> log.info("DB 검색 완료 (락 획득 실패): {}", response.getMessage()))
                                .doOnError(e -> log.error("DB 검색 중 에러 발생 (락 획득 실패)", e))
                                .onErrorReturn(RestaurantResponse.failure("서비스 중 오류가 발생했습니다. (DB 대체)"));
                    } else { // Lock was acquired, proceed with normal flow and release lock in doFinally
                        return validateRequest(request)
                                .flatMap(this::searchWithFallback)
                                .flatMap(tuple -> processSearchResult(tuple.getT1(), tuple.getT2(), request.getQuery(), request.getUserId()))
                                .doOnSuccess(response -> log.info("검색 완료: {}", response.getMessage()))
                                .doOnError(e -> log.error("에러 발생", e))
                                .onErrorReturn(RestaurantResponse.failure("서비스 중 오류가 발생했습니다."))
                                .doFinally(signalType -> {
                                    redisLockManager.releaseLock(lockKey, acquiredValue).subscribe();
                                });
                    }
                });
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

    private Mono<RestaurantResponse> processSearchResult(List<Restaurant> restaurants, String dataSource, String query, String userId) {
        if (restaurants == null || restaurants.isEmpty()) {
            return Mono.just(RestaurantResponse.failure("검색 결과를 찾을 수 없습니다."));
        }

        Mono<Void> writeTask = "DATABASE".equals(dataSource)
            ? increaseKeywordCount(query)
            : saveResultsAndUpdateKeyword(restaurants, query);

        return writeTask.thenReturn(RestaurantResponse.success(restaurants, dataSource))
                .doOnSuccess(response -> {
                    kafkaProducerService.sendRestaurantSearchEvent(new RestaurantSearchEvent(this, query, userId, restaurants, dataSource));
                });
    }

    private Mono<ApiCallResult> callExternalApis(RestaurantSearchRequest request) {
        return naverApiClient.search(request)
                .subscribeOn(apiScheduler)
                .onErrorResume(naverError -> {
                    log.warn("네이버 API 호출 실패, 카카오 fallback 시도", naverError);
                    return kakaoApiClient.search(request)
                            .onErrorResume(kakaoError -> {
                                log.error("카카오 API 호출도 실패", kakaoError);
                                return Mono.just(ApiCallResult.failure("ALL_API", "네이버/카카오 API 모두 실패"));
                            });
                })
                .flatMap(naverResult -> {
                    if (naverResult.hasResults()) {
                        return Mono.just(naverResult);
                    } else if (naverResult.isSuccess()) {
                        return kakaoApiClient.search(request)
                                .subscribeOn(apiScheduler)
                                .onErrorResume(kakaoError -> {
                                    log.error("카카오 API 호출 실패", kakaoError);
                                    return Mono.just(naverResult);
                                })
                                .map(kakaoResult -> kakaoResult.hasResults() ? kakaoResult : naverResult);
                    } else {
                        return Mono.just(ApiCallResult.failure("NAVER", "네이버 API 실패"));
                    }
                });
    }

    private Mono<List<Restaurant>> searchFromDatabase(RestaurantSearchRequest request) {
        return Mono.fromCallable(() -> {
            try {
                log.info("DB에서 검색 시작: {}", request.getQuery());
                return repository.findByQuery(request.getQuery());
            } catch (Exception e) {
                log.error("DB 검색 중 오류", e);
                return Collections.<Restaurant>emptyList();
            }
        }).subscribeOn(jdbcScheduler);
    }

    public Mono<Void> saveResultsAndUpdateKeyword(List<Restaurant> restaurants, String query) {
        return Mono.fromRunnable(() -> transactionService.saveResultsAndUpdateKeywordBlocking(restaurants, query))
                .subscribeOn(jdbcScheduler)
                .then();
    }

    private Mono<Void> increaseKeywordCount(String query) {
        // PopularKeywordService를 통해 카운트 증가
        return Mono.fromRunnable(() -> {
            String region = popularKeywordService.extractRegionFromKeyword(query);
            popularKeywordService.increaseCount(query, region);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}

