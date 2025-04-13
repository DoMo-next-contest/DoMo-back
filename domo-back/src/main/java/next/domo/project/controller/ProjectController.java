package next.domo.project.controller;

import lombok.RequiredArgsConstructor;
import next.domo.project.dto.*;
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
    public ResponseEntity<String> createProject(@RequestBody ProjectCreateRequestDto requestDto) {
        projectService.createProject(requestDto);
        return ResponseEntity.ok("프로젝트 생성 성공!");
    }

    @Operation(summary = "모든 프로젝트 리스트 조회 (이름, 태그명, 데드라인)")
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

    @Operation(summary = "가장 최근 접속한 프로젝트 조회 (이름, 태그명)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "최근 프로젝트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "최근 프로젝트 조회 실패")
    })
    @GetMapping("/recent")
    public ResponseEntity<ProjectRecentResponseDto> getRecentProject() {
        ProjectRecentResponseDto recent = projectService.getRecentProject();
        return ResponseEntity.ok(recent);
    }
}
