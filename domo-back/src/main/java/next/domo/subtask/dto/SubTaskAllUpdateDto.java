package next.domo.subtask.dto;

import lombok.Getter;

@Getter
public class SubTaskAllUpdateDto {
    private Long subTaskId;
    private String subTaskName;
    private int subTaskOrder;
    private int subTaskExpectedTime;
}
