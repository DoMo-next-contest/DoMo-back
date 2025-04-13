package next.domo.project.service;

import lombok.RequiredArgsConstructor;
import next.domo.project.dto.*;
import next.domo.project.entity.Project;
import next.domo.project.entity.ProjectTag;
import next.domo.project.repository.ProjectRepository;
import next.domo.project.repository.ProjectTagRepository;
import next.domo.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTagRepository projectTagRepository;
    private final UserService userService;

    public void createProject(ProjectCreateRequestDto requestDto) {
        Long userId = userService.getCurrentUserId();

        ProjectTag tag = projectTagRepository.findByProjectTagNameAndUserId(requestDto.getProjectTagName(), userId)
                .orElseThrow(() -> new RuntimeException("해당 태그가 없습니다."));

        Project project = Project.builder()
                .userId(userId)
                .projectTagId(tag.getProjectTagId())
                .projectName(requestDto.getProjectName())
                .projectDescription(requestDto.getProjectDescription())
                .projectRequirement(requestDto.getProjectRequirement())
                .projectDeadline(requestDto.getProjectDeadline())
                .projectExpectedTime(0) // 하위 작업으로 합산 예정
                .build();

        projectRepository.save(project);
    }

    public List<ProjectListResponseDto> getAllProjects() {
        Long userId = userService.getCurrentUserId();
        return projectRepository.findByUserId(userId).stream()
                .map(project -> {
                    ProjectTag tag = projectTagRepository.findById(project.getProjectTagId())
                            .orElseThrow(() -> new RuntimeException("태그를 찾을 수 없습니다."));
                    return new ProjectListResponseDto(
                            project.getProjectId(),
                            project.getProjectName(),
                            tag.getProjectTagName(),
                            project.getProjectDeadline()
                    );
                }).collect(Collectors.toList());
    }

    public ProjectDetailResponseDto getProjectDetail(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        ProjectTag tag = projectTagRepository.findById(project.getProjectTagId())
                .orElseThrow(() -> new RuntimeException("태그를 찾을 수 없습니다."));
        return new ProjectDetailResponseDto(
                project.getProjectName(),
                project.getProjectDescription(),
                tag.getProjectTagName(),
                project.getProjectDeadline()
        );
    }

    public void updateProject(Long projectId, ProjectUpdateRequestDto requestDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 입력된 값만 업데이트
        if (requestDto.getProjectName() != null) {
            project.setProjectName(requestDto.getProjectName());
        }
        if (requestDto.getProjectDescription() != null) {
            project.setProjectDescription(requestDto.getProjectDescription());
        }
        if (requestDto.getProjectRequirement() != null) {
            project.setProjectRequirement(requestDto.getProjectRequirement());
        }
        if (requestDto.getProjectDeadline() != null) {
            project.setProjectDeadline(requestDto.getProjectDeadline());
        }
        if (requestDto.getProjectTagName() != null) {
            ProjectTag tag = projectTagRepository.findByProjectTagNameAndUserId(requestDto.getProjectTagName(), project.getUserId())
                    .orElseThrow(() -> new RuntimeException("해당 태그를 찾을 수 없습니다."));
            project.setProjectTagId(tag.getProjectTagId());
        }
    }
    public void deleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }

    public ProjectRecentResponseDto getRecentProject() {
        Long userId = userService.getCurrentUserId();
        Project recent = projectRepository.findTopByUserIdOrderByProjectIdDesc(userId)
                .orElseThrow(() -> new RuntimeException("최근 프로젝트가 없습니다."));

        ProjectTag tag = projectTagRepository.findById(recent.getProjectTagId())
                .orElseThrow(() -> new RuntimeException("태그를 찾을 수 없습니다."));

        return new ProjectRecentResponseDto(
                recent.getProjectName(),
                tag.getProjectTagName()
        );
    }
}

