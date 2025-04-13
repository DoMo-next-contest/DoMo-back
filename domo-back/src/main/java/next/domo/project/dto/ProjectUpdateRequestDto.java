package next.domo.project.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ProjectUpdateRequestDto {
    private String projectName;
    private String projectDescription;
    private String projectRequirement;
    private String projectTagName;
    private LocalDateTime projectDeadline;
}
