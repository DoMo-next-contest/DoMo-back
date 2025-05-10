package next.domo.gpt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import next.domo.gpt.service.GPTService;
import next.domo.project.entity.ProjectLevelType;
import next.domo.user.service.UserService;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gpt")
public class GPTController {
    private final GPTService gptService;
    private final UserService userService;

    @Operation(summary = "GPT로 하위작업 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GPT로 하위작업 생성 성공"),
            @ApiResponse(responseCode = "4XX", description = "GPT로 하위작업 생성 실패")
    })
    @PostMapping("/{projectId}/subtasks")
    public String createSubTaskByGPT(HttpServletRequest request,  @Parameter(description = "하위작업 생성할 프로젝트 ID", required = true, example = "1") @PathVariable Long projectId) {
        Long userId = userService.getUserIdFromToken(request);
        return gptService.createSubTaskByGPT(userId, projectId);
    }

    @Operation(summary = "GPT로 프로젝트 난이도 예측 및 저장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로젝트 난이도 예측 성공"),
            @ApiResponse(responseCode = "4XX", description = "예측 실패")
    })
    @PostMapping("/{projectId}/predict-level")
    public ProjectLevelType predictProjectLevelByGPT(HttpServletRequest request,
                                                     @Parameter(description = "난이도 예측할 프로젝트 ID", required = true, example = "1")
                                                     @PathVariable Long projectId) {
        Long userId = userService.getUserIdFromToken(request);
        return gptService.predictProjectLevelByGPT(projectId);
    }
}
