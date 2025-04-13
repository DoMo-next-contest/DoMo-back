package next.domo.project.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectCreateRequestDto {
    private String projectName;
    private String projectDescription;
    private String projectRequirement;
    private LocalDateTime projectDeadline;
    private String projectTagName;
}
