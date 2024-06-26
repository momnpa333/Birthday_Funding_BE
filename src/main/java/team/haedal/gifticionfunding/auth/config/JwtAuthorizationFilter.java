package team.haedal.gifticionfunding.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import team.haedal.gifticionfunding.auth.jwt.JwtProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        log.info("dofilterinternal 실행");

        if (request.getRequestURI().startsWith("/oauth2") || request.getRequestURI().startsWith("/refresh") ||request.getRequestURI().startsWith("/swagger-ui")||request.getRequestURI().startsWith("/api-docs")||request.getRequestURI().startsWith("/v3")) {
            log.info("다음필터 실행");

            filterChain.doFilter(request, response);
            return;
        }

        //authorization header에서 access token을 추출
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = jwtProvider.parseAccessToken(authHeader);

        if (StringUtils.hasText(accessToken) && jwtProvider.validateToken(accessToken)) {
            // 일단 USER 권한만 부여
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            Authentication authentication = new UsernamePasswordAuthenticationToken(jwtProvider.getUserIdFromToken(accessToken), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            throw new IllegalArgumentException("예상치 못한 토큰 오류");
        }

        log.info("다음필터 실행");
        // 다음 Filter를 실행하기 위한 코드. 마지막 필터라면 필터 실행 후 리소스를 반환한다.
        filterChain.doFilter(request, response);
    }
}
