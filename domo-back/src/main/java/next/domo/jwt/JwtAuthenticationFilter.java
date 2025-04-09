package next.domo.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import next.domo.user.User;
import next.domo.user.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> NO_CHECK_URLS = List.of("/api/user/login", "/swagger-ui", "/api/user/signup", "/v3/api-docs", "/oauth2/google/login", "/oauth2/kakao/login", "/oauth2/apple/login");

    public final JwtProvider jwtProvider;
    public final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUrl = request.getRequestURI();

        // NO_CHECK_URLS에 해당 requestUrl이 있는 경우
        // JWT 검증 로직 없이 filter 통과
        if (NO_CHECK_URLS.stream().anyMatch(requestUrl::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken= jwtProvider.extractAccessToken(request).orElse(null);
        String refreshToken = jwtProvider.extractRefreshToken(request).orElse(null);

        try{
            // token이 비어있음
            if(accessToken == null && refreshToken == null) {
                log.error("액세스 토큰과 리프레시 토큰이 모두 존재하지 않습니다.");
                throw new JWTVerificationException("액세스 토큰과 리프레시 토큰이 모두 존재하지 않습니다.");
            }
            // accesstoken 유효
            if(accessToken != null && jwtProvider.isAccessTokenValid(accessToken)) {
                checkAccessTokenAndAuthentication(request, response, filterChain);
                return;
            }
            // refreshtoken 유효 -> accesstoken 재발급
            if(refreshToken != null && jwtProvider.isRefreshTokenValid(refreshToken)) {
                reIssueAccessToken(response, refreshToken);
                return;
            }
        }
        catch (JWTVerificationException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            String errorMessage = String.format("{\"errorCode\": \"%d\", \"error\": \"%s\"}", e.getMessage());
            response.getWriter().write(errorMessage);
        }

    }

    public void reIssueAccessToken(HttpServletResponse response, String refreshToken) throws IOException {
        log.info("리프레시토큰이 유효하나 DB에 있는지는 모름. DB에서 찾아봐서 없으면 예외 발생할 것임.");
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new JWTVerificationException("유효하지 않은 리프레시 토큰입니다."));
        jwtProvider.sendAccessToken(response, jwtProvider.createAccessToken(user.getLoginId(), user.getUserId()));
    }

    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");
        jwtProvider.extractAccessToken(request)
                .ifPresent(accessToken -> {
                    jwtProvider.extractUserId(accessToken)
                            .ifPresent(userId -> userRepository.findByUserId(userId)
                                    .ifPresent(this::saveAuthentication)
                            );
                });

        filterChain.doFilter(request, response);
    }

    // 인증 허가
    public void saveAuthentication(User myUser) {
        String password = myUser.getPassword();

        UserDetails userDetailsUser = org.springframework.security.core.userdetails.User.builder()
                .username(myUser.getLoginId())
                .password(password)
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetailsUser, null,
                        authoritiesMapper.mapAuthorities(userDetailsUser.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
