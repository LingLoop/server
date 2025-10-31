package song.lingloop.server.common.error.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    USER_EXIST(HttpStatus.BAD_REQUEST,"이미 회원가입한 이메일 입니다"),
    USER_NOT_FOUND (HttpStatus.NOT_FOUND, "회원이 없습니다"),
    BAD_USER(HttpStatus.BAD_REQUEST, "5회이상 이메일 인증을 시도하였습니다. 6시간 이후 다시 시도해주세요"),
    UN_AUTHENTICATED_USER(HttpStatus.BAD_REQUEST, "권한이 없는 회원입니다"),

    EMAIL_USER_NOT_FOUND (HttpStatus.NOT_FOUND, "이메일로 가입된 회원이 없습니다"),
    EMAIL_NOT_SENT(HttpStatus.BAD_REQUEST, "인증 이메일 발송에 실패하였습니다"),
    EMAIL_EXPIRED(HttpStatus.NOT_FOUND, "인증 이메일 유효기간이 지났습니다"),
    EMAIL_INVALID(HttpStatus.BAD_REQUEST, "이메일 인증 값이 유효하지 않습니다"),

    LOGIN_FAIL(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다"),
    TOKEN_NOT_EXIST(HttpStatus.NOT_FOUND, "토큰이 없습니다"),
    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "token expired"),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "token invalid");

    private final HttpStatus httpStatus;
    private final String message;
}