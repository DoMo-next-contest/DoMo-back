package next.domo.project.service;

import lombok.RequiredArgsConstructor;
import next.domo.project.dto.*;
import next.domo.project.entity.Project;
import next.domo.project.entity.ProjectLevelType;
import next.domo.project.entity.ProjectStatus;
import next.domo.project.entity.ProjectTag;
import next.domo.project.repository.ProjectRepository;
import next.domo.project.repository.ProjectTagRepository;
import next.domo.subtask.repository.SubTaskRepository;
import next.domo.user.entity.User;
import next.domo.user.repository.UserRepository;
import next.domo.user.service.UserService;
import next.domo.user.service.UserTagService;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.lang.IllegalStateException;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTagRepository projectTagRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SubTaskRepository subTaskRepository;
    private final UserTagService userTagService;

    public Long createProject(ProjectCreateRequestDto requestDto) {
        Long userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        ProjectTag tag = projectTagRepository.findByProjectTagNameAndUserUserId(requestDto.getProjectTagName(), userId)
                .orElseThrow(() -> new RuntimeException("해당 태그가 없습니다."));

        Project project = Project.builder()
                .user(user)
                .projectTag(tag)
                .projectName(requestDto.getProjectName())
                .projectDescription(requestDto.getProjectDescription())
                .projectRequirement(requestDto.getProjectRequirement() != null ? requestDto.getProjectRequirement() : "")
                .projectDeadline(requestDto.getProjectDeadline())
                .projectExpectedTime(0) // 하위 작업으로 합산 예정
                .projectProgressRate(0)
                .projectStatus(ProjectStatus.IN_PROGRESS)
                .build();

        projectRepository.save(project);
        return project.getProjectId();
    }

    public List<ProjectListResponseDto> getAllProjects() {
        Long userId = userService.getCurrentUserId();
        return projectRepository.findByUserUserId(userId).stream()
                .map(project -> new ProjectListResponseDto(
                        project.getProjectId(),
                        project.getProjectName(),
                        project.getProjectTag().getProjectTagName(),
                        project.getProjectDeadline(),
                        project.getProjectProgressRate(),
                        project.getProjectDescription()
                )).collect(Collectors.toList());
    }


    public ProjectDetailResponseDto getProjectDetail(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));
        return new ProjectDetailResponseDto(
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectTag().getProjectTagName(),
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
            project.setProjectTag(tag);
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

        return new ProjectRecentResponseDto(
                recent.getProjectName(),
                recent.getProjectTag().getProjectTagName()
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

    public int completeAndRewardProject(Project project) {
        // ✅ 하위작업 존재 여부 확인
        int subTaskCount = subTaskRepository.countAllByProjectId(project.getProjectId());
        if (subTaskCount == 0) {
            throw new IllegalStateException("하위작업이 존재하지 않으므로 프로젝트를 완료할 수 없습니다.");
        }

        // 저장된 난이도 계수 사용
        Integer levelFactorInt = project.getProjectLevel(); // 60, 50, 40 중 하나
        if (levelFactorInt == null) throw new IllegalStateException("프로젝트 난이도가 설정되지 않았습니다.");

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
        double levelFactor = levelFactorInt / 100.0;
        int coin = (int) (baseScore * levelFactor * expectedHours);

        // 저장
        project.setProjectCoin(coin);
        project.markAsDone();
        projectRepository.save(project);

        // 6. 유저에게 코인 지급
        User user = project.getUser();
        user.addCoin(coin);
        userRepository.save(user);

        //  사용자 하위작업 태그 예측 대비 소요율 갱신
        userTagService.updateUserTagRates(user);

        return coin;
    }

    public List<ProjectListResponseDto> getCompletedProjects() {
        Long userId = userService.getCurrentUserId();
        return projectRepository.findByUserUserId(userId).stream()
                .filter(project -> project.getProjectStatus() == ProjectStatus.DONE)
                .map(project -> new ProjectListResponseDto(
                        project.getProjectId(),
                        project.getProjectName(),
                        project.getProjectTag().getProjectTagName(),
                        project.getProjectDeadline(),
                        project.getProjectProgressRate(),
                        project.getProjectDescription()
                )).collect(Collectors.toList());
    }

    public List<ProjectListResponseDto> getProjectListResponses(List<Long> projectTagIds, String sortBy) {
        Sort sort = switch (sortBy) {
            case "name" -> Sort.by("projectName").ascending();
            case "progress" -> Sort.by("projectProgressRate").descending();
            case "deadline" -> Sort.by("projectDeadline").ascending();
            default -> Sort.unsorted();
        };

        List<Project> projects;

        if (projectTagIds != null && !projectTagIds.isEmpty()) {
            projects = projectRepository.findByProjectTag_ProjectTagIdIn(projectTagIds, sort);
        } else {
            projects = projectRepository.findAll(sort);
        }

        return projects.stream()
                .map(ProjectListResponseDto::from)
                .toList();
    }

}

