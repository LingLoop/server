package song.lingloop.server.common.error.handler;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import song.lingloop.server.common.error.errorcode.ErrorCode;
import song.lingloop.server.common.error.exception.BusinessException;

import java.time.LocalDateTime;

import static song.lingloop.server.common.error.errorcode.CommonErrorCode.INVALID_PARAMETER;


@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), e.getMessage());
        pd.setProperty("timestamp", LocalDateTime.now());

        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBusinessException(MethodArgumentNotValidException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(INVALID_PARAMETER.getHttpStatus(), INVALID_PARAMETER.getMessage());

        e.getBindingResult().getFieldErrors()
                .forEach(error -> problemDetail.setProperty(error.getField(), error.getDefaultMessage()));

        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return problemDetail;
    }
}
