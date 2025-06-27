package kr.hhplus.be.server.infrastructure.external;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import java.time.Duration;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class RedisLockManager {
    
    private final StringRedisTemplate redisTemplate;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

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
}
