package song.lingloop.server.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import song.lingloop.server.common.util.JwtUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Builder
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader("Authorization");
        String token;

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = accessToken.split(" ")[1];
        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response state code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(token);

        if (!category.equals("access")) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response state code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        List<Long> crews = new ArrayList<>();
        if(jwtUtil.getCrews(token).length()>1)
            crews = Arrays.stream(jwtUtil.getCrews(token).split(" "))
                .map(Long::parseLong)
                .toList();

        CustomUserDetails customUserDetails
         = CustomUserDetails.builder()
                .seq(jwtUtil.getSeq(token))
                .nickname(jwtUtil.getNickname(token))
                .email(jwtUtil.getEmail(token))
                .genre(jwtUtil.getGenre(token))
                .gender(jwtUtil.getGender(token))
                .year(jwtUtil.getYear(token))
                .region(jwtUtil.getRegion(token))
                .position(jwtUtil.getPosition(token))
                .crews(crews)
                .role(null)
                .build();

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
