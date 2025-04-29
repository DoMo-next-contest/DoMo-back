package next.domo.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import next.domo.user.entity.User;
import next.domo.user.service.UserService;
import next.domo.user.service.UserTagService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-tag")
@SecurityRequirement(name = "accessToken")
public class UserTagController {

    private final UserService userService;
    private final UserTagService userTagService;

    @Operation(summary = "사용자의 하위작업 태그별 예측 대비 소요율 갱신",
    description = "사용자가 완료한 하위작업(SubTask) 데이터를 기반으로, 각 하위작업 태그(SubTaskTag)별 평균 예측 대비 소요율(actualTime / expectedTime)을 계산하여 UserTag 테이블에 저장 또는 갱신합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "하위작업 태그별 예측 대비 소요율 갱신 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 사용자 요청")
    })
    @PutMapping("/update")
    public ResponseEntity<String> updateUserTagRates(HttpServletRequest request) {
        Long userId = userService.getUserIdFromToken(request);
        User user = userService.getUserById(userId);
        
        try {
            userTagService.updateUserTagRates(user);
            return ResponseEntity.ok("UserTag 예측 대비 소요율 갱신 완료!");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("하위작업 데이터가 없습니다: " + e.getMessage());
        }
    }
}
