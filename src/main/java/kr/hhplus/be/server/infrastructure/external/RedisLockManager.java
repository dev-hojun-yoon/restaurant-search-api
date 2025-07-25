package kr.hhplus.be.server.infrastructure.external;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisLockManager {
    
    private final StringRedisTemplate redisTemplate;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    // 동기락
    public boolean lock(String key, String value) {
        Boolean result = redisTemplate.opsForValue()
                            .setIfAbsent(key, value, TIMEOUT);
        return Boolean.TRUE.equals(result);
    }

    public void unlock(String key, String value) {
        String script = """
                if redis.call("get", KEYS[1]) == ARGV[1] then
                    return redis.call("del", KEYS[1])
                else
                    return 0
                end
                """;
        
        redisTemplate.execute(
            RedisScript.of(script, Long.class),
            Collections.singletonList(key),
            value
        );
    }

    // 리액티브 방식의 락
    public Mono<String> acquireLock(String key, Duration timeout) {
        String lockValue = UUID.randomUUID().toString();

        return Mono.fromCallable(() -> {
            Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, lockValue, TIMEOUT);
            return Boolean.TRUE.equals(acquired) ? lockValue : null;
        })
        .subscribeOn(Schedulers.boundedElastic()); // blocking 연산
    }

    public Mono<Void> releaseLock(String key, String value) {
        String script = """
                if redis.call("get", KEYS[1]) == ARGV[1] then
                    return redis.call("del", KEYS[1])
                else
                    return 0
                end
                """;
        
        return Mono.fromCallable(() -> 
            redisTemplate.execute(
                RedisScript.of(script, Long.class),
                Collections.singletonList(key),
                value
            )
        )
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }
}
