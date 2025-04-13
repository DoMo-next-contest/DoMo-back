package next.domo.user;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import next.domo.user.UserService;
import next.domo.user.dto.ChangePasswordRequestDto;
import next.domo.user.dto.UserLoginRequestDto;
import next.domo.user.dto.UserSignUpRequestDto;

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
}
