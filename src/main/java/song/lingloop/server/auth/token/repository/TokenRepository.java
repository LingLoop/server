package song.lingloop.server.auth.token.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TokenRepository {
    private final String PREFIX_REFRESHTOKEN = "rt:"; // key값이 중복되지 않도록 상수 선언
    private final int LIMIT_TIME = 60 * 60 * 24 * 365; // 인증번호 유효 시간

    private final StringRedisTemplate stringRedisTemplate;

    // 발급 후 Redis 저장
    public void createRefreshToken(Long id, String refreshToken) {

        stringRedisTemplate.opsForValue()
                .set(PREFIX_REFRESHTOKEN + id, refreshToken, Duration.ofSeconds(LIMIT_TIME));
    }

    // id에 해당하는 token 불러오기
    public String getRefreshToken(Long id) {
        return stringRedisTemplate.opsForValue().get(PREFIX_REFRESHTOKEN + id);
    }
    // id에 해당하는 token 불러오기
    public boolean isRefreshTokenVaild(Long id, String token) {
//        log.info("redis : {}", stringRedisTemplate.opsForValue().get(PREFIX_REFRESHTOKEN + id));
//        log.info("equals : {}", stringRedisTemplate.opsForValue().get(PREFIX_REFRESHTOKEN + id).equals(token));
        return hasKey(id) && stringRedisTemplate.opsForValue().get(PREFIX_REFRESHTOKEN + id).equals(token);
    }
    // Redis에 존재하는지 확인
    public boolean hasKey(Long id) {
        return stringRedisTemplate.hasKey(PREFIX_REFRESHTOKEN + id);
    }
}