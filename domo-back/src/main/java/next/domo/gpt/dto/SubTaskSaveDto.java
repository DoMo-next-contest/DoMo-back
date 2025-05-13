package next.domo.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTaskSaveDto {
    private Long projectId;
    private List<GptSubTaskDto> subTasks;
}