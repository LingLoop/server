// song/lingloop/server/mvp/controller/MatchController.java
package song.lingloop.server.mvp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import song.lingloop.server.mvp.dto.MatchRequest;
import song.lingloop.server.mvp.dto.MatchResult;
import song.lingloop.server.mvp.service.MatchService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchResult> requestMatch(@RequestBody MatchRequest req) {
        return ResponseEntity.ok(matchService.findOrWaitForMatch(req));
    }

    @GetMapping("/check")
    public ResponseEntity<MatchResult> checkMatch(@RequestParam String userId) {
        return ResponseEntity.ok(matchService.checkStatus(userId));
    }

    @PostMapping("/ack")
    public ResponseEntity<Void> ack(@RequestBody MatchRequest req) {
        matchService.acknowledgeJoin(req.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leave(@RequestBody MatchRequest req) {
        matchService.leave(req);
        return ResponseEntity.ok().build();
    }
}
