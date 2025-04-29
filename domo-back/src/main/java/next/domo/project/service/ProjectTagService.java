package next.domo.project.service;

import lombok.RequiredArgsConstructor;
import next.domo.user.entity.User;
import next.domo.user.repository.UserRepository;
import next.domo.user.service.UserService;

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
    private final UserRepository userRepository;

    public void createProjectTag(String projectTagName) {
        Long userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        if (projectTagName == null || projectTagName.trim().isEmpty()) {
            throw new IllegalArgumentException("프로젝트 태그 이름이 비어있습니다.");
        }

        boolean exists = projectTagRepository.existsByProjectTagNameAndUserUserId(projectTagName, userId);
        if (exists) {
            throw new RuntimeException("이미 존재하는 태그입니다.");
        }


        ProjectTag tag = ProjectTag.builder()
                .user(user)
                .projectTagName(projectTagName)
                .build();
        projectTagRepository.save(tag);
    }

    public List<ProjectTagResponseDto> getMyProjectTags() {
        Long userId = userService.getCurrentUserId();
        return projectTagRepository.findByUserUserId(userId).stream()
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
