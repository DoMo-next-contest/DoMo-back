package next.domo.subtask.dto;

import lombok.Getter;
import next.domo.subtask.entity.SubTaskTag;

@Getter
public class SubTaskUpdateDto {
    private String subTaskName;
    private int subTaskOrder;
    private int subTaskExpectedTime;
}
