package next.domo.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import next.domo.subtask.entity.SubTask;
import next.domo.subtask.entity.SubTaskTag;
import next.domo.subtask.repository.SubTaskRepository;
import next.domo.user.entity.User;
import next.domo.user.entity.UserTag;
import next.domo.user.repository.UserTagRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTagService {

    private final UserTagRepository userTagRepository;
    private final SubTaskRepository subTaskRepository;

    public void updateUserTagRates(User user) {
        List<SubTask> subTasks = subTaskRepository.findAllByUserId(user.getUserId());

        if (subTasks.isEmpty()) {
            throw new IllegalStateException("사용자가 생성한 하위작업이 존재하지 않습니다.");
        }

        // SubTaskTag별 그룹핑
        Map<SubTaskTag, List<SubTask>> tagToTasks = subTasks.stream()
                .filter(st -> st.getSubTaskExpectedTime() != null && st.getSubTaskActualTime() != null)
                .collect(Collectors.groupingBy(SubTask::getSubTaskTag));

        for (SubTaskTag tag : SubTaskTag.values()) {
            List<SubTask> tasksForTag = tagToTasks.get(tag);

            // 이 태그의 기록이 아예 없다면 생략
            if (tasksForTag == null || tasksForTag.isEmpty()) {
                continue; // 기록 없는 태그는 저장하지 않음
            }

            float averageRate = (float) tasksForTag.stream()
            .mapToDouble(st -> {
                int expected = st.getSubTaskExpectedTime();
                int actual = st.getSubTaskActualTime();
                return expected <= 0 ? 1.0 : (double) actual / expected;
            })
            .average()
            .orElse(1.0);

            // 이미 존재하면 업데이트, 아니면 생성
            Optional<UserTag> existing = userTagRepository.findByUserAndSubTaskTag(user, tag);

            if (existing.isPresent()) {
                UserTag userTag = existing.get();
                userTag.setActualToExpectedRate(averageRate);
            } else {
                UserTag newUserTag = UserTag.builder()
                        .user(user)
                        .subTaskTag(tag)
                        .actualToExpectedRate(averageRate)
                        .build();
                userTagRepository.save(newUserTag);
            }
        }
    }
}

