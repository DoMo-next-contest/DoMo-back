package next.domo.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import next.domo.user.dto.ChangePasswordRequestDto;
import next.domo.user.dto.UserLoginRequestDto;
import next.domo.user.dto.UserOnboardingRequestDto;
import next.domo.user.dto.UserSignUpRequestDto;
import next.domo.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @SecurityRequirement(name = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "회원가입 실패 (아이디/이메일 중복)")
    })
    @PostMapping("/signup")
    public String signup(@RequestBody UserSignUpRequestDto requestDto, HttpServletResponse response) {
        userService.signUp(requestDto, response);
        return "회원가입 성공!";
    }

    @Operation(summary = "로그인")
    @SecurityRequirement(name = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "로그인 실패 (아이디/비밀번호 불일치)")
    })
    @PostMapping("/login")
    public String login(@RequestBody UserLoginRequestDto requestDto, HttpServletResponse response) {
        userService.login(requestDto, response);
        return "로그인 성공!";
    }

    @Operation(summary = "비밀번호 변경")
    @SecurityRequirement(name = "accessToken")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "비밀번호 변경 실패 (기존 비밀번호 불일치)")
    })
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequestDto requestDto) {
        userService.changePassword(requestDto);
        return ResponseEntity.ok("비밀번호 변경 성공");
    }
    
    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "accessToken")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "회원 탈퇴 실패 (해당 아이디 없음)")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.ok("회원 탈퇴 성공");
    }

    @Operation(summary = "온보딩 설문조사 제출")
    @SecurityRequirement(name = "accessToken")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "설문 제출 성공"),
        @ApiResponse(responseCode = "400", description = "설문 제출 실패")
    })
    @PostMapping("/onboarding")
    public ResponseEntity<String> submitOnboardingSurvey(
        HttpServletRequest request,
        @RequestBody UserOnboardingRequestDto requestDto
        ) {
            Long userId = userService.getUserIdFromToken(request);
            userService.submitOnboardingSurvey(userId, requestDto);
            return ResponseEntity.ok("온보딩 설문조사 완료!");
        }

    @Operation(summary = "아이템 뽑기", description = "보유 코인 50 차감 후 아이템 1개를 뽑습니다. 코인이 부족할 경우 실패합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "아이템 뽑기 성공"),
        @ApiResponse(responseCode = "400", description = "코인 부족으로 인한 뽑기 실패")
    })
    @PutMapping("/draw")
    @SecurityRequirement(name = "accessToken")
    public ResponseEntity<String> drawItem(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        try {
            userService.drawItem(userId);
            return ResponseEntity.ok("아이템 뽑기 성공!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "보유 코인 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "보유 코인 조회 성공"),
        @ApiResponse(responseCode = "400", description = "조회 실패")
    })
    @GetMapping("/coin")
    @SecurityRequirement(name = "accessToken")
    public ResponseEntity<Integer> getUserCoin(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        int coin = userService.getUserCoin(userId);
        return ResponseEntity.ok(coin);
    }
}
