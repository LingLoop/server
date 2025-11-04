package song.lingloop.server.mvp.dto;

public record MatchResult(
        boolean matched,
        String roomId,
        String selfId,
        String opponentId
) {}