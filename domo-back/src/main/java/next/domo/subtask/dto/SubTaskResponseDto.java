package next.domo.subtask.dto;

import lombok.Builder;
import lombok.Getter;
import next.domo.subtask.entity.SubTaskTag;

@Getter
@Builder
public class SubTaskResponseDto {
    private Long subTaskId;
    private int subTaskOrder;
    private String subTaskName;
    private int subTaskExpectedTime;
    private int subTaskActualTime;
    private boolean subTaskIsDone;
    private SubTaskTag subTaskTag;
}
