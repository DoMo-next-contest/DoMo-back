package next.domo.subtask.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import next.domo.subtask.dto.SubTaskCreateDto;
import next.domo.subtask.dto.SubTaskResponseDto;
import next.domo.subtask.dto.SubTaskTimeDto;
import next.domo.subtask.dto.SubTaskUpdateDto;
import next.domo.subtask.service.SubTaskService;
import next.domo.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subtasks")
public class SubTaskController {

    private final UserService userService;
    private final SubTaskService subTaskService;

    // subtask 추가
    @Operation(summary = "하위작업 추가",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "추가할 하위작업 JSON 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "{\n \"projectId\": \"1\", \n \"subTaskName\": \"전체적인 계획 짜기\", \n \"subTaskExpectedTime\": 60, \n \"subTaskTag\": \"DOCUMENTATION\"  }"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하위작업 추가 성공"),
            @ApiResponse(responseCode = "4XX", description = "하위작업 추가 실패")
    })
    @PostMapping("")
    public ResponseEntity<String> createSubTask(HttpServletRequest request, @RequestBody SubTaskCreateDto subTaskCreateDto) {
        Long userId = userService.getUserIdFromToken(request);
        subTaskService.addSubTask(userId, subTaskCreateDto);
        return ResponseEntity.ok("하위작업 추가 성공");
    }

    // subtask 조회
    @Operation(summary = "특정 프로젝트의 하위작업 조회",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "조회시 요청 JSON 데이터 없음"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "특정 프로젝트의 하위작업 조회 성공"),
            @ApiResponse(responseCode = "4XX", description = "특정 프로젝트의 하위작업 조회 실패")
    })
    @GetMapping("/{projectId}/subtasks")
    public ResponseEntity<List<SubTaskResponseDto>> showSubTask(HttpServletRequest request, @Parameter(description = "조회할 프로젝트 ID", required = true, example = "1") @PathVariable Long projectId) {
        Long userId = userService.getUserIdFromToken(request);
        List<SubTaskResponseDto> subTaskList = subTaskService.showSubTask(userId, projectId);
        return ResponseEntity.ok(subTaskList);
    }

    // subtask 수정
    @Operation(summary = "하위작업 수정",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 하위작업 JSON 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "{\n \"subTaskName\": \"일정 수정하기\", \n \"subTaskExpectedTime\": 30, \n \"subTaskTag\": \"PLANNING_STRATEGY\"  }"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하위작업 수정 성공"),
            @ApiResponse(responseCode = "4XX", description = "하위작업 수정 실패")
    })
    @PutMapping("/{subTaskId}")
    public ResponseEntity<String> updateSubTask(HttpServletRequest request, @Parameter(description = "수정할 하위작업 ID", required = true, example = "1") @PathVariable Long subTaskId, @RequestBody SubTaskUpdateDto subTaskUpdateDto) {
        Long userId = userService.getUserIdFromToken(request);
        subTaskService.updateSubTask(userId, subTaskId, subTaskUpdateDto);
        return ResponseEntity.ok("하위작업 업데이트 성공");
    }

    // subtask 삭제
    @Operation(summary = "하위작업 삭제",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "삭제시 요청 JSON 데이터 없음"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하위작업 삭제 성공"),
            @ApiResponse(responseCode = "4XX", description = "하위작업 삭제 실패")
    })
    @DeleteMapping("/{subTaskId}")
    public ResponseEntity<String> deleteSubTask(HttpServletRequest request, @Parameter(description = "삭제할 하위작업 ID", required = true, example = "1") @PathVariable Long subTaskId) {
        Long userId = userService.getUserIdFromToken(request);
        subTaskService.deleteSubTask(userId, subTaskId);
        return ResponseEntity.ok("하위작업 삭제 성공");

    }

    // subtask 시간 저장
    @Operation(summary = "하위작업 실제시간 저장",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "하위 작업 실제 시간 JSON 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "{\n \"subTaskActualTime\": 40  }"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하위작업 실제시간 저장 성공"),
            @ApiResponse(responseCode = "4XX", description = "하위작업 실제시간 저장 실패")
    })
    @PostMapping("/{subTaskId}/time")
    public ResponseEntity<String> saveSubTaskTime(HttpServletRequest request, @Parameter(description = "시간을 저장할 하위작업 ID", required = true, example = "1") @PathVariable Long subTaskId, @RequestBody SubTaskTimeDto subTaskTimeDto){
        Long userId = userService.getUserIdFromToken(request);
        subTaskService.saveSubTaskTime(userId, subTaskId, subTaskTimeDto);
        return ResponseEntity.ok("하위작업 시간 저장 성공");
    }

    // project 별로 subtask 수정사항 한번에 저장
    @Operation(summary = "프로젝트 별로 하위작업 수정사항 한번에 저장",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프로젝트 별 수정한 하위작업 리스트",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "[\n {\n \"subTaskId\": 1,\n \"subTaskName\": \"API 명세 작성\",\n \"subTaskExpectedTime\": 60,\n \"subTaskActualTime\": 45,\n \"subTaskTag\": \"DOCUMENT\",\n \"subTaskOrder\": 1\n },\n {\n \"subTaskId\": 2,\n \"subTaskName\": \"디자인 회의\",\n \"subTaskExpectedTime\": 30,\n \"subTaskActualTime\": 35,\n \"subTaskTag\": \"MEETING\",\n \"subTaskOrder\": 2\n }\n ]"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로젝트 별로 하위작업 수정사항 한번에 저장 성공"),
            @ApiResponse(responseCode = "4XX", description = "프로젝트 별로 하위작업 수정사항 한번에 저장 실패")
    })
    @PutMapping("/{projectId}/subtasks")
    public ResponseEntity<String> updateSubTaskByProject(HttpServletRequest request, @Parameter(description = "하위작업을 수정할 프로젝트 ID", required = true, example = "1") @PathVariable Long projectId, @RequestBody List<SubTaskUpdateDto> subTaskUpdateDtos){
        Long userId = userService.getUserIdFromToken(request);
        subTaskService.updateSubTaskByProject(userId, projectId, subTaskUpdateDtos);
        return ResponseEntity.ok("프로젝트 별로 하위작업 수정사항 한번에 저장 성공");
    }

    @Operation(summary = "하위작업 완료",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "완료시 요청 JSON 데이터 없음"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하위작업 완료 성공"),
            @ApiResponse(responseCode = "4XX", description = "하위작업 완료 실패")
    })
    // subtask 완료 저장
    @PutMapping("/{subTaskId}/done")
    public ResponseEntity<String> doneSubTask(HttpServletRequest request, @Parameter(description = "완료할 하위작업 ID", required = true, example = "1") @PathVariable Long subTaskId){
        Long userId = userService.getUserIdFromToken(request);
        subTaskService.doneSubTask(userId, subTaskId);
        return ResponseEntity.ok("하위작업 완료 성공");
    }

}
