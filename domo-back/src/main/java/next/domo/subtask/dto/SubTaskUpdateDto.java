package next.domo.subtask.dto;

import lombok.Getter;
import next.domo.subtask.entity.SubTaskTag;

@Getter
public class SubTaskUpdateDto {
    private Long subTaskId;
    private String subTaskName;
    private int subTaskOrder;
    private int subTaskExpectedTime;
    private SubTaskTag subTaskTag;
}
