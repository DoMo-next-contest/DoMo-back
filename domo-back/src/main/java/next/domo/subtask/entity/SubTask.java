package next.domo.subtask.entity;

import jakarta.persistence.*;
import lombok.*;
import next.domo.project.entity.Project;
import next.domo.subtask.dto.SubTaskTimeDto;
import next.domo.subtask.dto.SubTaskUpdateDto;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Entity
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subTaskId;

    private String subTaskName;

    private int subTaskExpectedTime;

    private int subTaskActualTime;

    @Enumerated(EnumType.STRING)
    private SubTaskTag subTaskTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    public void updateSubTask(SubTaskUpdateDto subTaskUpdateDto) {
        this.subTaskName = subTaskUpdateDto.getSubTaskName();
        this.subTaskExpectedTime = subTaskUpdateDto.getSubTaskExpectedTime();
        this.subTaskTag = subTaskUpdateDto.getSubTaskTag();
    }

    public void saveSubTaskTime(SubTaskTimeDto subTaskTimeDto) {
        this.subTaskActualTime = subTaskTimeDto.getSubTaskActualTime();
    }
}
