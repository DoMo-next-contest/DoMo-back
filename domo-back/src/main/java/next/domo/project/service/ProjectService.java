package next.domo.project.service;

import lombok.RequiredArgsConstructor;
import next.domo.project.dto.*;
import next.domo.project.entity.Project;
import next.domo.project.entity.ProjectLevelType;
import next.domo.project.entity.ProjectTag;
import next.domo.project.repository.ProjectRepository;
import next.domo.project.repository.ProjectTagRepository;
import next.domo.subtask.repository.SubTaskRepository;
import next.domo.user.User;
import next.domo.user.UserRepository;
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
    private final UserRepository userRepository;
    private final SubTaskRepository subTaskRepository;

    public void createProject(ProjectCreateRequestDto requestDto) {
        Long userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        ProjectTag tag = projectTagRepository.findByProjectTagNameAndUserUserId(requestDto.getProjectTagName(), userId)
                .orElseThrow(() -> new RuntimeException("해당 태그가 없습니다."));

        Project project = Project.builder()
                .user(user)
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
        return projectRepository.findByUserUserId(userId).stream()
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
            ProjectTag tag = projectTagRepository.findByProjectTagNameAndUserUserId(
                requestDto.getProjectTagName(), 
                project.getUser().getUserId()
            ).orElseThrow(() -> new RuntimeException("해당 태그를 찾을 수 없습니다."));
            project.setProjectTagId(tag.getProjectTagId());
        }

        projectRepository.save(project);
    }

    public void deleteProject(Long projectId) {
    if (!projectRepository.existsById(projectId)) {
        throw new RuntimeException("존재하지 않는 프로젝트입니다.");
    }
    projectRepository.deleteById(projectId);
}

    public ProjectRecentResponseDto getRecentProject() {
        Long userId = userService.getCurrentUserId();
        Project recent = projectRepository.findTopByUserUserIdOrderByProjectIdDesc(userId)
                .orElseThrow(() -> new RuntimeException("최근 프로젝트가 없습니다."));

        ProjectTag tag = projectTagRepository.findById(recent.getProjectTagId())
                .orElseThrow(() -> new RuntimeException("태그를 찾을 수 없습니다."));

        return new ProjectRecentResponseDto(
                recent.getProjectName(),
                tag.getProjectTagName()
        );
    }

    public void updateProjectExpectedTime(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
    
        Integer totalExpectedTime = subTaskRepository.sumExpectedTimeByProjectId(projectId);
        if (totalExpectedTime == null) totalExpectedTime = 0; // null 방지
    
        project.setProjectExpectedTime(totalExpectedTime);
        projectRepository.save(project);
    }

    public void updateProjectProgressRate(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
    
        Integer total = subTaskRepository.countAllByProjectId(projectId);
        Integer done = subTaskRepository.countDoneByProjectId(projectId);
        
        if (total == null || total == 0) {
            project.setProjectProgressRate(0);
        } else {
            int progressRate = (done == null ? 0 : done * 100 / total);
            project.setProjectProgressRate(progressRate);
        }
        
        projectRepository.save(project);
    }

    public Project getProjectEntityById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
    }

    public void completeAndRewardProject(Project project, ProjectLevelType levelType) {
        // 난이도 계수 설정
        project.setProjectLevel(levelType.getFactor());

        // 예상 소요 시간 (분 → 시간으로 환산)
        Integer expectedMinutes = project.getProjectExpectedTime();
        if (expectedMinutes == null) expectedMinutes = 0;
        double expectedHours = expectedMinutes / 60.0;

        // 기본 점수 결정
        int baseScore = 10;
        if (expectedMinutes > 1440) {
            baseScore = 8;
        } else if (expectedMinutes > 720) {
            baseScore = 9;
        }

        // 난이도 계수 적용 + 코인 계산
        double levelFactor = levelType.getFactor() / 100.0;
        int coin = (int) (baseScore * levelFactor * expectedHours);

        // 저장
        project.setProjectCoin(coin);
        project.markAsDone();
        projectRepository.save(project);
}

}

