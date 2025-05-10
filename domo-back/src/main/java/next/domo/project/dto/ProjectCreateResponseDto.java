package next.domo.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectCreateResponseDto {
    private Long projectId;
    private String projectDescription;
    private Integer projectProgressRate;
}