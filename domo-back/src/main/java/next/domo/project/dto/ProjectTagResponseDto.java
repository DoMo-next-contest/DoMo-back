package next.domo.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectTagResponseDto {
    private Long projectTagId;
    private String projectTagName;
}