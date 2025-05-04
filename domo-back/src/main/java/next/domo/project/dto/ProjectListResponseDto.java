package next.domo.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import next.domo.project.entity.Project;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProjectListResponseDto {
    private Long projectId;
    private String projectName;
    private String projectTagName;
    private LocalDateTime projectDeadline;

    public static ProjectListResponseDto from(Project project) {
        return ProjectListResponseDto.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .projectTagName(project.getProjectTag().getProjectTagName())
                .projectDeadline(project.getProjectDeadline())
                .build();
    }
}
