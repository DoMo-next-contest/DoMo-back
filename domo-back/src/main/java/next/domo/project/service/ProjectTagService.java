package next.domo.project.service;

import lombok.RequiredArgsConstructor;
import next.domo.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import next.domo.project.dto.ProjectTagResponseDto;
import next.domo.project.entity.ProjectTag;
import next.domo.project.repository.ProjectTagRepository;
import next.domo.project.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectTagService {

    private final ProjectTagRepository projectTagRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public void createProjectTag(String projectTagName) {
        Long userId = userService.getCurrentUserId();

        if (projectTagName == null || projectTagName.trim().isEmpty()) {
            throw new IllegalArgumentException("프로젝트 태그 이름이 비어있습니다.");
        }

        boolean exists = projectTagRepository.existsByProjectTagNameAndUserId(projectTagName, userId);
        if (exists) {
            throw new RuntimeException("이미 존재하는 태그입니다.");
        }

        ProjectTag tag = ProjectTag.builder()
                .userId(userId)
                .projectTagName(projectTagName)
                .build();
        projectTagRepository.save(tag);
    }

    public List<ProjectTagResponseDto> getMyProjectTags() {
        Long userId = userService.getCurrentUserId();
        return projectTagRepository.findByUserId(userId).stream()
                .map(tag -> new ProjectTagResponseDto(tag.getProjectTagId(), tag.getProjectTagName()))
                .collect(Collectors.toList());
    }

    public void deleteProjectTag(Long projectTagId) {
        boolean hasProject = projectRepository.existsByProjectTagId(projectTagId);
    
        if (hasProject) {
            throw new RuntimeException("해당 태그를 사용하는 프로젝트가 있어 삭제할 수 없습니다. 먼저 프로젝트의 태그를 변경하세요.");
        }
    
        ProjectTag tag = projectTagRepository.findById(projectTagId)
                .orElseThrow(() -> new RuntimeException("프로젝트 태그를 찾을 수 없습니다."));
    
        projectTagRepository.delete(tag);
    }
}
