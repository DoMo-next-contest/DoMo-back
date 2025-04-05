package next.domo.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import next.domo.jwt.JwtProvider;
import next.domo.user.dto.ChangePasswordRequestDto;
import next.domo.user.dto.UserLoginRequestDto;
import next.domo.user.dto.UserSignUpRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public void signUp(UserSignUpRequestDto requestDto, HttpServletResponse response) {
        // 아이디 중복 체크
        if (userRepository.findByLoginId(requestDto.getLoginId()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        // 이메일 중복 체크
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 저장
        User user = User.builder()
                .loginId(requestDto.getLoginId())
                .password(passwordEncoder.encode(requestDto.getPassword())) // 비밀번호 암호화
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .build();

        userRepository.save(user);

        // 가입하자마자 로그인 토큰 발급
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
    }

    public void login(UserLoginRequestDto requestDto, HttpServletResponse response) {
        User user = userRepository.findByLoginId(requestDto.getLoginId())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken();
        jwtProvider.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
    }

    public void changePassword(String loginId, ChangePasswordRequestDto requestDto) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("해당 아이디를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("기존 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
   }

   public void deleteUser(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("해당 아이디를 찾을 수 없습니다."));

        userRepository.delete(user);
}
}
