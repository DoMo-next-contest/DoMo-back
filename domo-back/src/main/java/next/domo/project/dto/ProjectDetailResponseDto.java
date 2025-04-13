package next.domo.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectDetailResponseDto {
    private String projectName;
    private String projectDescription;
    private String projectTagName;
    private LocalDateTime projectDeadline;
}
