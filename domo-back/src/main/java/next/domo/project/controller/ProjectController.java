package next.domo.project.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import next.domo.project.dto.*;
import next.domo.project.entity.Project;
import next.domo.project.entity.ProjectLevelType;
import next.domo.project.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project")
@SecurityRequirement(name = "accessToken")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 생성 성공"),
        @ApiResponse(responseCode = "400", description = "프로젝트 생성 실패")
    })
    @PostMapping
    public ResponseEntity<Long> createProject(@RequestBody ProjectCreateRequestDto requestDto) {
        Long projectId = projectService.createProject(requestDto);
        return ResponseEntity.ok(projectId);
    }


    @Operation(summary = "모든 프로젝트 리스트 조회 (프로젝트Id, 프로젝트명, 태그명, 데드라인, 진행률, 설명, 완료여부)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 리스트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "프로젝트 리스트 조회 실패")
    })
    @GetMapping
    public ResponseEntity<List<ProjectListResponseDto>> getAllProjects() {
        List<ProjectListResponseDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "특정 프로젝트 상세 조회 (이름, 설명, 태그명, 데드라인)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 상세 조회 성공"),
        @ApiResponse(responseCode = "400", description = "프로젝트 상세 조회 실패")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponseDto> getProjectDetail(@PathVariable Long projectId) {
        ProjectDetailResponseDto detail = projectService.getProjectDetail(projectId);
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "프로젝트 수정 (이름, 설명, 요구사항, 태그명, 데드라인 수정)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 수정 성공"),
        @ApiResponse(responseCode = "400", description = "프로젝트 수정 실패")
    })
    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId, @RequestBody ProjectUpdateRequestDto requestDto) {
        projectService.updateProject(projectId, requestDto);
        return ResponseEntity.ok("프로젝트 수정 성공!");
    }

    @Operation(summary = "프로젝트 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "프로젝트 삭제 실패")
    })
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok("프로젝트 삭제 성공!");
    }

    @Operation(summary = "가장 최근 접속한 미완료 프로젝트 조회 (프로젝트Id, 프로젝트명, 태그명, 데드라인, 진행률, 설명, 완료여부)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "최근 프로젝트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "최근 프로젝트 조회 실패")
    })
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentProject() {
        ProjectListResponseDto recent = projectService.getRecentProject();
        if (recent == null) {
            return ResponseEntity.ok("최근 접속한 미완료 프로젝트가 없습니다.");
        }
        return ResponseEntity.ok(recent);
    }

    @Operation(summary = "프로젝트 예상 소요 시간 반영")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예상 소요 시간 반영 성공"),
        @ApiResponse(responseCode = "400", description = "예상 소요 시간 반영 실패")
    })
    @PutMapping("/{projectId}/expected-time")
    public ResponseEntity<String> updateProjectExpectedTime(@PathVariable Long projectId) {
        projectService.updateProjectExpectedTime(projectId);
        return ResponseEntity.ok("예상 소요 시간 반영 성공!");
    }

    @Operation(summary = "프로젝트 진행률 계산 및 반영")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "진행률 계산 성공"),
        @ApiResponse(responseCode = "400", description = "진행률 계산 실패"),
        @ApiResponse(responseCode = "404", description = "해당 ID의 프로젝트가 존재하지 않음")
    })
    @PutMapping("/{projectId}/progress-rate")
    public ResponseEntity<String> updateProgressRate(@PathVariable Long projectId) {
        projectService.updateProjectProgressRate(projectId);
        return ResponseEntity.ok("프로젝트 진행률 계산 완료!");
    }

    @Operation(
        summary = "프로젝트 완료 처리 및 보상 계산",
        description = "모든 하위작업이 완료된 프로젝트에 대해 GPT가 저장한 난이도(projectLevel)를 기반으로 코인을 계산하여 프로젝트를 최종 완료 처리합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로젝트 완료 및 코인 계산 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 프로젝트 상태 오류"),
        @ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없습니다")
    })
    @PutMapping("/{projectId}/complete")
    public ResponseEntity<ProjectCompletionResponseDto> completeAndRewardProject(@PathVariable Long projectId) {
        Project project = projectService.getProjectEntityById(projectId);
        int coin = projectService.completeAndRewardProject(project);
        return ResponseEntity.ok(new ProjectCompletionResponseDto("프로젝트 완료 및 코인 계산 성공!", coin));
    }

    @Operation(summary = "완료된 프로젝트 리스트 조회 (프로젝트Id, 프로젝트명, 태그명, 데드라인, 진행률, 설명, 완료여부)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "완료된 프로젝트 리스트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "완료된 프로젝트 리스트 조회 실패")
    })
    @GetMapping("/completed")
    public ResponseEntity<List<ProjectListResponseDto>> getCompletedProjects() {
        List<ProjectListResponseDto> completedProjects = projectService.getCompletedProjects();
        return ResponseEntity.ok(completedProjects);
    }

    @Operation(summary = "프로젝트 tag별 필터링 및 정렬",
            description = """
            프로젝트를 정렬 기준(name, progress, deadline)에 따라 정렬하고 선택한 태그 ID 리스트(tagIds)에 해당하는 프로젝트만 필터링합니다.
            여러 개의 tagIds를 넘기려면 ?tagIds=1&tagIds=2 형식으로 요청하세요. ex) /projects?tagIds=1&tagIds=3&sortBy=deadline
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로젝트 tag별 필터링 및 정렬 성공"),
            @ApiResponse(responseCode = "4XX", description = "프로젝트 tag별 필터링 및 정렬 실패")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<ProjectListResponseDto>> getProjectsByProjectTagAndSort(
            @Parameter(
                    description = "선택할 프로젝트 태그 ID 리스트 (여러 개 가능)",
                    example = "1, 2"
            )@RequestParam(required = false) List<Long> projectTagIds,
            @Parameter(
                    description = "정렬 기준 (name, progress, deadline 중 하나)",
                    example = "progress"
            ) @RequestParam(required = false) String sortBy
    ) {
        return ResponseEntity.ok(projectService.getProjectListResponses(projectTagIds, sortBy));
    }
}
