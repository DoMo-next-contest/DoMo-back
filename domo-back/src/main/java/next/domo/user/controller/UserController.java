package next.domo.user.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import next.domo.user.dto.*;
import next.domo.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "사용자 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아이템 뽑기 성공"),
            @ApiResponse(responseCode = "400", description = "코인 부족으로 인한 뽑기 실패")
    })
    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> showUserInfo(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        UserInfoDto userInfo = userService.showUserInfo(userId);
        return ResponseEntity.ok(userInfo);
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

    @Operation(summary = "캐릭터 파일 업로드", description = "GLB 파일을 S3에 업로드하고 URL을 User 테이블에 저장합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "캐릭터 파일 업로드 성공"),
        @ApiResponse(responseCode = "400", description = "캐릭터 파일 업로드 실패")
    })
    @PostMapping(value = "/character/upload", consumes = "multipart/form-data")
    @SecurityRequirement(name = "accessToken")
    public ResponseEntity<String> uploadCharacterFile(
        HttpServletRequest request,
        @RequestPart MultipartFile file
    ) {
        Long userId = userService.getUserIdFromToken(request);
        String fileUrl = userService.uploadCharacterFile(userId, file);
        return ResponseEntity.ok(fileUrl);
    }

    @Operation(summary = "캐릭터 파일 URL 조회", description = "로그인된 사용자의 캐릭터 GLB 파일 URL을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "캐릭터 파일 URL 반환 성공"),
        @ApiResponse(responseCode = "400", description = "사용자 정보 없음 또는 URL 없음")
    })
    @GetMapping("/character/url")
    @SecurityRequirement(name = "accessToken")
    public ResponseEntity<String> getCharacterFileUrl(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        String url = userService.getCharacterFileUrl(userId);
        
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body("캐릭터 파일이 아직 업로드되지 않았습니다.");
        }
        
        return ResponseEntity.ok(url);
    }

    @Operation(summary = "예상소요시간 계산 선호도 수정",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 예상소요시간 계산 선호도 JSON 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "{\n \"workPace\": \"TIGHT\"  }"
                            )
                    )
            ))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예상소요시간 계산 선호도 수정 성공"),
            @ApiResponse(responseCode = "400", description = "예상소요시간 계산 선호도 수정 실패")
    })
    @PatchMapping("/users/work-pace")
    public ResponseEntity<Void> updateWorkPace(HttpServletRequest request, @RequestBody UpdateWorkPaceRequestDto updateWorkPaceRequestDto) {
        Long userId = userService.getUserIdFromToken(request);
        userService.updateWorkPace(userId, updateWorkPaceRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "세부화 선호도 수정",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "세부화 선호도 JSON 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "{\n \"detailPreference\": \"MANY_TASKS\"  }"
                            )
                    )
            ))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "세부화 선호도 수정 성공"),
            @ApiResponse(responseCode = "400", description = "세부화 선호도 수정 실패")
    })
    @PatchMapping("/users/detail-preference")
    public ResponseEntity<Void> updateDetailPreference(HttpServletRequest request, @RequestBody UpdateDetailPreferenceRequestDto updateDetailPreferenceRequestDto) {
        Long userId = userService.getUserIdFromToken(request);
        userService.updateDetailPreference(userId, updateDetailPreferenceRequestDto);
        return ResponseEntity.ok().build();
    }
}
