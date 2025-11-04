package song.lingloop.server.mvp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import song.lingloop.server.mvp.dto.MatchRequest;
import song.lingloop.server.mvp.dto.MatchResult;
import song.lingloop.server.mvp.repository.RedisMatchRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final RedisMatchRepository redisRepo;

    private static String pair(String a, String b) { return a + "-" + b; }

    @Transactional
    public MatchResult findOrWaitForMatch(MatchRequest req) {
        final String userId = req.userId();
        final String myPair  = pair(req.nativeLang(), req.learningLang());
        final String oppPair = pair(req.learningLang(), req.nativeLang());

        log.info("[match] {} : findOrWaitForMatch ({}->{})", userId, req.nativeLang(), req.learningLang());

        // 이미 매칭되어 있으면 그대로 반환
        MatchResult existing = redisRepo.getMatch(userId);
        if (existing != null) {
            log.info("[match] {} : existing match -> room={}", userId, existing.roomId());
            return existing;
        }

        // 내 흔적(양 방향) 청소
        redisRepo.removeMatch(userId);
        redisRepo.removeFromWaiting(myPair, userId);
        redisRepo.removeFromWaiting(oppPair, userId);

        // 반대 큐에서 유효 후보 POP (유령 방지)
        String opponent = redisRepo.popValidWaiting(oppPair, 16); // 최대 16개 스캔

        // 없으면 내 큐에 대기
        if (opponent == null || opponent.equals(userId)) {
            boolean queued = redisRepo.addWaitingIfAbsent(myPair, userId);
            log.info("[match] {} queued={} pair={}", userId, queued, myPair);
            return new MatchResult(false, null, userId, null);
        }

        // 매칭 성사
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        MatchResult r1 = new MatchResult(true, roomId, userId, opponent);
        MatchResult r2 = new MatchResult(true, roomId, opponent, userId);

        redisRepo.saveMatch(userId, r1);
        redisRepo.saveMatch(opponent, r2);

        // 혹여 남은 흔적들 정리
        redisRepo.removeFromWaiting(myPair, userId);
        redisRepo.removeFromWaiting(oppPair, userId);
        redisRepo.removeFromWaiting(myPair, opponent);
        redisRepo.removeFromWaiting(oppPair, opponent);

        log.info("[match] paired: {}({}) ↔ {}({}) room={}", userId, myPair, opponent, oppPair, roomId);
        return r1;
    }

    @Transactional(readOnly = true)
    public MatchResult checkStatus(String userId) {
        MatchResult result = redisRepo.getMatch(userId);
        if (result != null) {
            log.info("[match-check] {} -> room={}", userId, result.roomId());
            return result;
        }
        return new MatchResult(false, null, userId, null);
    }

    @Transactional
    public void acknowledgeJoin(String userId) {
        redisRepo.removeMatch(userId);
        log.info("[match-ack] {} match key removed", userId);
    }

    @Transactional
    public void leave(MatchRequest req) {
        final String userId = req.userId();
        final String myPair  = pair(req.nativeLang(), req.learningLang());
        final String oppPair = pair(req.learningLang(), req.nativeLang());

        redisRepo.removeMatch(userId);
        redisRepo.removeFromWaiting(myPair, userId);
        redisRepo.removeFromWaiting(oppPair, userId);

        log.info("[leave] {} cleaned pairs=({}, {})", userId, myPair, oppPair);
    }
}
