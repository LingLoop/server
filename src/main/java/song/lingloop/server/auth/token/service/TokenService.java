package song.lingloop.server.auth.token.service;


import com.nimbusds.openid.connect.sdk.claims.Gender;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import song.lingloop.server.auth.dto.UserDto;
import song.lingloop.server.auth.token.repository.TokenRepository;
import song.lingloop.server.common.error.exception.BusinessException;
import song.lingloop.server.common.util.JwtUtil;

import static song.lingloop.server.common.error.errorcode.AuthErrorCode.TOKEN_EXPIRED;
import static song.lingloop.server.common.error.errorcode.AuthErrorCode.TOKEN_INVALID;
import static song.lingloop.server.common.error.errorcode.AuthErrorCode.TOKEN_NOT_EXIST;



@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;

    public void createRefreshToken(Long id, String refreshToken) {
        tokenRepository.createRefreshToken(id, refreshToken);
    }

    public UserDto getUserDto(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token;
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new BusinessException(TOKEN_NOT_EXIST);
        else token = authorization.split(" ")[1];
        if(jwtUtil.isExpired(token)){
            throw new BusinessException(TOKEN_EXPIRED, "토큰이 만료되었습니다");
        }else if(!jwtUtil.getCategory(token).equals("access")){
            throw new BusinessException(TOKEN_INVALID, "유효하지 않은 토큰입니다");
        }

        return UserDto.builder()
                .seq(jwtUtil.getSeq(token))
                .nickname(jwtUtil.getNickname(token))
                .build();
    }
    // access 만료 후 refresh 통한 access 요청시 refresh 함께 새로 내림
    public void reissueProcess(HttpServletRequest request, HttpServletResponse response){
        String refreshToken = findRefreshToken(request);
        String accessToken = request.getHeader("Authorization");

        if (accessToken == null || !accessToken.startsWith("Bearer ")|| refreshToken == null) {
            //response state code
            throw new BusinessException(TOKEN_NOT_EXIST, "요청에 토큰이 입력되지 않았습니다");
        }else if(jwtUtil.isExpired(refreshToken)){
            throw new BusinessException(TOKEN_EXPIRED, "토큰이 만료되었습니다");
        }else if(!jwtUtil.getCategory(refreshToken).equals("refresh")){
            log.info("category : {}", jwtUtil.getCategory(refreshToken));
            throw new BusinessException(TOKEN_INVALID, "유효하지 않은 토큰입니다");
        }else if(!tokenRepository.isRefreshTokenVaild(jwtUtil.getSeq(accessToken.split(" ")[1]), refreshToken)){
            log.info("access token id : {}", jwtUtil.getSeq(accessToken.split(" ")[1]));
            log.info("refresh token id : {}", refreshToken);
            throw new BusinessException(TOKEN_INVALID, "유효하지 않은 토큰입니다");
        }

        String email = jwtUtil.getEmail(refreshToken);
        String nickname = jwtUtil.getNickname(refreshToken);
        Long seq = jwtUtil.getSeq(refreshToken);
        Position position = jwtUtil.getPosition(refreshToken);
        Genre genre = jwtUtil.getGenre(refreshToken);
        Gender gender = jwtUtil.getGender(refreshToken);
        Region region = jwtUtil.getRegion(refreshToken);
        String year = jwtUtil.getYear(refreshToken);
        String crews = jwtUtil.getCrews(refreshToken);


        //make new JWTz
        String newAccess = jwtUtil.createJwt("access", nickname, seq, email,
                position, gender, genre, year, region, crews, 86400000L);
        String newRefresh = jwtUtil.createJwt("refresh", nickname, seq, email,
                position, gender, genre, year, region, crews, 86400000L);

        //response
        tokenRepository.createRefreshToken(seq, newRefresh);
        response.addHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

    }
    private String findRefreshToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                return cookie.getValue();
            }
        }
        return null;
    }
    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
