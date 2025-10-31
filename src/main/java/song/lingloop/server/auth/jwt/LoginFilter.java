package song.lingloop.server.auth.jwt;

import com.e106.reco.domain.artist.entity.Genre;
import com.e106.reco.domain.artist.entity.Position;
import com.e106.reco.domain.artist.entity.Region;
import com.e106.reco.domain.artist.user.dto.CustomUserDetails;
import com.e106.reco.domain.artist.user.entity.Gender;
import com.e106.reco.global.auth.token.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenRepository refreshTokenRepository;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String email = request.getParameter("email");
        String password = obtainPassword(request);
        log.info("Attempting to authenticate email: {}", email);
        log.info("Attempting to authenticate password: {}", password);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        log.info("authToken: {}", authToken);
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String nickname = customUserDetails.getNickname();
        String email = customUserDetails.getEmail();
        Long seq = customUserDetails.getSeq();

        Region region = customUserDetails.getRegion();
        Position position = customUserDetails.getPosition();
        Gender gender = customUserDetails.getGender();
        String year = customUserDetails.getYear();
        Genre genre = customUserDetails.getGenre();

        StringBuilder sb = new StringBuilder();
        customUserDetails.getCrews().forEach(crew -> sb.append(crew).append(" "));
        log.info("crews: {}", sb);
        if(sb.length()>1) sb.deleteCharAt(sb.length()-1);

        String crews = sb.toString();
        System.out.print(crews);
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//        GrantedAuthority auth = iterator.next();

        // 토큰 생성
        String accessToken = jwtUtil.createJwt("access", nickname, seq, email,
                position, gender, genre, year, region, crews, 86400000L);
        String refreshToken = jwtUtil.createJwt("refresh", nickname, seq, email,
                position, gender, genre, year, region, crews, 86400000L);

        // 토큰 응답 설정
        // 세션에 사용자 ID 저장
        request.getSession().setAttribute("CURRENT_USER_ID", seq);
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());

        refreshTokenRepository.createRefreshToken(seq, refreshToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60*10);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
