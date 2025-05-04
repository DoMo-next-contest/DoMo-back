package next.domo.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import next.domo.jwt.JwtProvider;
import next.domo.project.entity.ProjectTag;
import next.domo.project.repository.ProjectTagRepository;
import next.domo.user.dto.*;
import next.domo.user.entity.User;
import next.domo.user.enums.OnboardingTagType;
import next.domo.user.enums.TaskDetailPreference;
import next.domo.user.enums.WorkPace;
import next.domo.user.repository.UserRepository;
import next.domo.upload.service.S3Service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ProjectTagRepository projectTagRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public Long getUserIdFromToken(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization").substring(7); // "Bearer "를 제외한 토큰
        return jwtProvider.extractUserId(accessToken).orElseThrow(() -> new RuntimeException("토큰에서 유저 아이디를 찾을 수 없습니다."));
    }

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

    public void changePassword(ChangePasswordRequestDto requestDto) {
        Long userId = getCurrentUserId();
    
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    
        if (!passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("기존 비밀번호가 일치하지 않습니다.");
        }
    
        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteUser() {
        Long userId = getCurrentUserId();
    
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));
    
        userRepository.delete(user);
    }

    public Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginId = userDetails.getUsername();
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("로그인 정보를 찾을 수 없습니다."));
        return user.getUserId();
    }

    @Transactional
    public void submitOnboardingSurvey(Long userId, UserOnboardingRequestDto requestDto) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        
        user.updateDetailPreference(requestDto.getDetailPreference());
        user.updateWorkPace(requestDto.getWorkPace());
        
        // 관심 태그 기반 project tag 자동 생성
        for (OnboardingTagType tagEnum : requestDto.getInterestedTags()) {
            String tagName = tagEnum.name();
            boolean exists = projectTagRepository.existsByProjectTagNameAndUserUserId(tagName, userId);
            
            if (!exists) {
                ProjectTag tag = ProjectTag.builder()
                .user(user)
                .projectTagName(tagName)
                .build();
                projectTagRepository.save(tag);
            }
        }
        
        userRepository.save(user);
    }

    @Transactional
    public void drawItem(Long userId) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (user.getUserCoin() < 50) {
            throw new IllegalArgumentException("코인이 부족하여 뽑기를 진행할 수 없습니다.");
        }

        user.useCoin(50);

        // TODO: 아이템 생성 로직 (랜덤 or 지정 방식)

        userRepository.save(user);
    }

    @Transactional
    public int getUserCoin(Long userId) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return user.getUserCoin();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public String uploadCharacterFile(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        String fileUrl = s3Service.uploadFile(file, "character"); // 폴더 지정
        user.setCharacterFileUrl(fileUrl);
        userRepository.save(user);
        
        return fileUrl;
    }

    public String getCharacterFileUrl(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return user.getCharacterFileUrl();
    }

    public void updateWorkPace(Long userId, UpdateWorkPaceRequestDto updateWorkPaceRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateWorkPace(updateWorkPaceRequestDto.getWorkPace());
    }

    public void updateDetailPreference(Long userId, UpdateDetailPreferenceRequestDto updateDetailPreferenceRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateDetailPreference(updateDetailPreferenceRequestDto.getDetailPreference());
    }
}
