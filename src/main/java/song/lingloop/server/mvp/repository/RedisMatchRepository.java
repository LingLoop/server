package song.lingloop.server.mvp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import song.lingloop.server.mvp.dto.MatchResult;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisMatchRepository {

    private final StringRedisTemplate stringRedis;

    @Qualifier("matchResultRedisTemplate")
    private final RedisTemplate<String, MatchResult> matchRedis;

    private static final String NS = "v1:";

    private static String qKey(String pair)                 { return NS + "waiting:" + pair; }              // List
    private static String fKey(String pair, String userId)  { return NS + "flag:" + pair + ":" + userId; }  // String
    private static String mKey(String userId)               { return NS + "match:" + userId; }              // MatchResult

    /** 대기 등록 (중복 방지 + TTL) */
    public boolean addWaitingIfAbsent(String pair, String userId) {
        // flag를 먼저 (SETNX) 잡고…
        Boolean placed = stringRedis.opsForValue().setIfAbsent(fKey(pair, userId), "1", Duration.ofMinutes(10));

        if (Boolean.TRUE.equals(placed)) {
            // 큐에 enqueue
            stringRedis.opsForList().rightPush(qKey(pair), userId);
            return true;
        }
        return false;
    }

    /** POP + 유효성 검증 + 소유권 확보
     *  - 큐에서 뽑은 사용자에 대해 여전히 flag가 있는지 확인
     *  - flag가 없으면 (유령/만료/이미 매칭됨) -> 버리고 계속 POP
     *  - flag가 있으면 즉시 flag 삭제(클레임)하고 반환
     */
    public String popValidWaiting(String pair, int maxScan) {
        String q = qKey(pair);
        for (int i = 0; i < maxScan; i++) {
            String cand = stringRedis.opsForList().leftPop(q);
            if (cand == null) return null;

            String fk = fKey(pair, cand);
            Boolean alive = stringRedis.hasKey(fk);
            if (alive) {
                // 소유권 확보: flag 제거
                stringRedis.delete(fk);
                return cand;
            }
        }
        return null;
    }

    /** 특정 사용자 대기 흔적 제거(큐 + flag) */
    public void removeFromWaiting(String pair, String userId) {
        if (pair == null || userId == null) return;
        stringRedis.opsForList().remove(qKey(pair), 0, userId);
        stringRedis.delete(fKey(pair, userId));
    }

    // 매치 결과 저장/조회/삭제
    public void saveMatch(String userId, MatchResult result) {
        matchRedis.opsForValue().set(mKey(userId), result, Duration.ofMinutes(10));
    }

    public MatchResult getMatch(String userId) {
        return matchRedis.opsForValue().get(mKey(userId));
    }

    public void removeMatch(String userId) {
        matchRedis.delete(mKey(userId));
    }
}
