package next.domo.gpt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import next.domo.gpt.service.GPTService;
import next.domo.user.service.UserService;
import next.domo.gpt.dto.GPTRequestDto;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gpt")
public class GPTController {
    private final GPTService gptService;
    private final UserService userService;

    @Operation(summary = "GPT로 하위작업 생성",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "하위작업 생성시 필요한 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "{\n \"projectName\": \"AI 기반 일정 관리 앱\",\n \"projectDescription\": \"사용자의 일정 데이터를 기반으로 하위 작업을 자동으로 생성하고 추천하는 앱\",\n \"projectRequirement\": \"Flutter와 Spring Boot를 활용한 앱 개발, JWT 기반 인증, AI 연동이 필수\",\n \"projectDeadline\": \"2025-05-31T23:59:00\"\n }"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GPT로 하위작업 생성 성공"),
            @ApiResponse(responseCode = "4XX", description = "GPT로 하위작업 생성 실패")
    })
    @PostMapping("/{projectId}/subtasks")
    public String createSubTaskByGPT(HttpServletRequest request, @Parameter(description = "하위작업을 생성할 프로젝트 ID", required = true, example = "1") @PathVariable Long projectId, @RequestBody GPTRequestDto GPTRequestDto) {
        Long userId = userService.getUserIdFromToken(request);
        return gptService.createSubTaskByGPT(userId, GPTRequestDto);
    }


}
