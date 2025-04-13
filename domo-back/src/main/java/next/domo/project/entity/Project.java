package next.domo.project.entity;

import jakarta.persistence.*;
import lombok.*;
import next.domo.user.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private Long userId;

    private Long projectTagId;

    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    @Column(columnDefinition = "TEXT")
    private String projectRequirement;

    private LocalDateTime projectDeadline;

    private Integer projectExpectedTime;

    private Integer projectLevel;

    private Integer projectCoin;

    public void updateProject(String projectName, String projectDescription, String projectRequirement, LocalDateTime projectDeadline, Long projectTagId) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectRequirement = projectRequirement;
        this.projectDeadline = projectDeadline;
        this.projectTagId = projectTagId;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }
    public void setProjectRequirement(String projectRequirement) {
        this.projectRequirement = projectRequirement;
    }
    public void setProjectDeadline(LocalDateTime projectDeadline) {
        this.projectDeadline = projectDeadline;
    }
    public void setProjectTagId(Long projectTagId) {
        this.projectTagId = projectTagId;
    }
}
