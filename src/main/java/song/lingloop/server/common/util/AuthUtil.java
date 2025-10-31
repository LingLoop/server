package song.lingloop.server.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import song.lingloop.server.common.error.errorcode.CommonErrorCode;
import song.lingloop.server.common.error.exception.BusinessException;

public class AuthUtil {

    public static CustomUserDetails getCustomUserDetails() {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
    }
}