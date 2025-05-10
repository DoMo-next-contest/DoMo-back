package next.domo.subtask.dto;

import lombok.Getter;
import next.domo.project.entity.Project;
import next.domo.subtask.entity.SubTask;
import next.domo.subtask.entity.SubTaskTag;

@Getter
public class SubTaskCreateDto {
    private String subTaskName;
    private int subTaskOrder;
    private int subTaskExpectedTime;

    public SubTask toEntity(Project project) {
        return SubTask.builder()
                .subTaskName(this.getSubTaskName())
                .subTaskOrder(this.getSubTaskOrder())
                .subTaskExpectedTime(this.getSubTaskExpectedTime())
                .subTaskIsDone(false)
                .subTaskTag(SubTaskTag.DEFAULT)
                .project(project)
                .build();
    }
}
