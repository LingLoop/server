package song.lingloop.server.mvp.dto;

public record MatchRequest(
        String userId,
        String nativeLang,
        String learningLang
) {}
