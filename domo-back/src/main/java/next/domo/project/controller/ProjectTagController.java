package next.domo.project.controller;

import lombok.RequiredArgsConstructor;
import next.domo.project.dto.ProjectTagCreateRequestDto;
import next.domo.project.dto.ProjectTagResponseDto;
import next.domo.project.service.ProjectTagService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/project-tag")
@SecurityRequirement(name = "accessToken")
public class ProjectTagController {

    private final ProjectTagService projectTagService;

    @Operation(summary = "프로젝트 태그 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 태그 생성 성공"),
        @ApiResponse(responseCode = "400", description = "프로젝트 태그 생성 실패 (중복 or 잘못된 입력)")
    })
    @PostMapping
    public ResponseEntity<String> createProjectTag(@RequestBody ProjectTagCreateRequestDto requestDto) {
        projectTagService.createProjectTag(requestDto.getProjectTagName());
        return ResponseEntity.ok("프로젝트 태그 생성 성공!");
    }

    @Operation(summary = "내 프로젝트 태그 목록 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 태그 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<ProjectTagResponseDto>> getMyProjectTags() {
        List<ProjectTagResponseDto> tags = projectTagService.getMyProjectTags();
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "프로젝트 태그 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로젝트 태그 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "프로젝트 태그 삭제 실패 (사용중인 프로젝트 존재)")
    })
    @DeleteMapping("/{projectTagId}")
    public ResponseEntity<String> deleteProjectTag(@PathVariable Long projectTagId) {
        projectTagService.deleteProjectTag(projectTagId);
        return ResponseEntity.ok("프로젝트 태그 삭제 성공!");
    }
}
