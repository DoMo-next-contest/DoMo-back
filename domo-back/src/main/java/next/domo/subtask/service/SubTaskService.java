package next.domo.subtask.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import next.domo.project.entity.Project;
import next.domo.project.repository.ProjectRepository;
import next.domo.project.service.ProjectService;
import next.domo.subtask.dto.*;
import next.domo.subtask.entity.SubTask;
import next.domo.subtask.repository.SubTaskRepository;
import next.domo.user.User;
import next.domo.user.UserRepository;
import next.domo.project.entity.ProjectLevelType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubTaskService {
    private final SubTaskRepository subTaskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    protected Project validateProjectOwner(Long userId, Long projectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디를 가진 사용자를 찾을 수 없습니다."));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디를 가진 프로젝트를 찾을 수 없습니다."));

        if (!project.getUser().equals(user)) {
            throw new IllegalArgumentException("해당 사용자는 이 프로젝트의 소유자가 아닙니다.");
        }
        return project;
    }

    protected SubTask validateSubTaskOwner(Long userId, Long subTaskId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디를 가진 사용자를 찾을 수 없습니다."));
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new EntityNotFoundException("해당 아이디를 가진 하위작업을 찾을 수 없습니다."));

        if (!subTask.getProject().getUser().equals(user)) {
            throw new IllegalArgumentException("해당 사용자는 이 하위작업의 소유자가 아닙니다.");
        }
        return subTask;
    }

    // subtask 생성 -> GPT


    // subtask 추가
    public void addSubTask(Long userId, Long projectId, SubTaskCreateDto subTaskCreateDto) {
        Project project = validateProjectOwner(userId, projectId);
        SubTask newSubTask = subTaskCreateDto.toEntity(project);
        subTaskRepository.save(newSubTask);
    }

    // project에 대한 subtask 조회
    public List<SubTaskResponseDto> showSubTask(Long userId, Long projectId) {
        Project project = validateProjectOwner(userId, projectId);

        List<SubTask> subTasks = subTaskRepository.findAllByProject(project);

        return subTasks.stream()
                .sorted(Comparator.comparingInt(SubTask::getSubTaskOrder))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // subtask 수정
    public void updateSubTask(Long userId, Long subTaskId, SubTaskUpdateDto subTaskUpdateDto) {
        SubTask subTask = validateSubTaskOwner(userId, subTaskId);
        subTask.updateSubTask(subTaskUpdateDto);
    }

    // subtask 삭제
    public void deleteSubTask(Long userId, Long subTaskId) {
        SubTask subTask = validateSubTaskOwner(userId, subTaskId);

        subTaskRepository.delete(subTask);
    }

    // subtask 시간 저장
    public void saveSubTaskTime(Long userId, Long subTaskId, SubTaskTimeDto subTaskTimeDto) {
        SubTask subTask = validateSubTaskOwner(userId, subTaskId);
        subTask.saveSubTaskTime(subTaskTimeDto);
    }

    // subtask 생성 후 한번에 저장
    public void createSubTaskByProject(Long userId, Long projectId, List<SubTaskCreateDto> subTaskCreateDtos) {
        Project project = validateProjectOwner(userId, projectId);

        for (SubTaskCreateDto dto : subTaskCreateDtos) {
            SubTask subTask = dto.toEntity(project);
            subTaskRepository.save(subTask);
        }

    }

    // subtask 수정사항 한번에 저장
    public void updateSubTaskByProject(Long userId, Long projectId, List<SubTaskUpdateDto> subTaskUpdateDtos) {
        Project project = validateProjectOwner(userId, projectId);
        List<SubTask> subTasks = subTaskRepository.findAllByProject(project);

        Map<Long, SubTaskUpdateDto> dtoMap = subTaskUpdateDtos.stream()
                .collect(Collectors.toMap(SubTaskUpdateDto::getSubTaskId, dto -> dto));

        for (SubTask subTask : subTasks) {
            SubTaskUpdateDto dto = dtoMap.get(subTask.getSubTaskId());
            if (dto != null) {
                subTask.updateSubTask(dto);
            }
        }

    }

    // subtask 완료
    public void doneSubTask(Long userId, Long subTaskId) {
        SubTask subTask = validateSubTaskOwner(userId, subTaskId);
        subTask.doneSubTask();
    
        Project project = subTask.getProject();
        List<SubTask> subTasks = subTaskRepository.findAllByProject(project);
        boolean allDone = subTasks.stream().allMatch(SubTask::isSubTaskIsDone);
    
        if (allDone) {
            project.markAsAlmostDone();
            projectRepository.save(project);
        }
    }

    // subtask tag 별 시간 계산


    private SubTaskResponseDto toDto(SubTask subTask) {
        return SubTaskResponseDto.builder()
                .subTaskId(subTask.getSubTaskId())
                .subTaskName(subTask.getSubTaskName())
                .subTaskOrder(subTask.getSubTaskOrder())
                .subTaskExpectedTime(subTask.getSubTaskExpectedTime())
                .subTaskActualTime(subTask.getSubTaskActualTime())
                .subTaskTag(subTask.getSubTaskTag())
                .subTaskIsDone(subTask.isSubTaskIsDone())
                .build();
    }
}
