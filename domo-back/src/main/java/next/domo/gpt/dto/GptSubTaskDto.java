package next.domo.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import next.domo.subtask.entity.SubTaskTag;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GptSubTaskDto {
    private int subTaskOrder;
    private String subTaskName;
    private int subTaskExpectedTime;
    private SubTaskTag subTaskTag;
}