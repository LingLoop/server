package song.lingloop.server.common.error.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid parameter included"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Cannot find resource."),
    WRITER_MISS_MACH(HttpStatus.BAD_REQUEST, "Only the author of the post must be able to modify the post"),
    JOIN_INPUT_FORMAT(HttpStatus.BAD_REQUEST, "Email conditions include @ or password conditions are at least 8 characters"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}