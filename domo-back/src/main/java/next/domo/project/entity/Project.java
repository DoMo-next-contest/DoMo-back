package next.domo.project.entity;

import jakarta.persistence.*;
import lombok.*;
import next.domo.user.entity.User;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_tag_id", nullable = false)
    private ProjectTag projectTag;

    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    @Column(columnDefinition = "TEXT")
    private String projectRequirement;

    private LocalDateTime projectDeadline;

    private Integer projectExpectedTime;

    private Integer projectLevel;

    private Integer projectCoin;

    public void updateProject(String projectName, String projectDescription, String projectRequirement, LocalDateTime projectDeadline, ProjectTag projectTag) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectRequirement = projectRequirement;
        this.projectDeadline = projectDeadline;
        this.projectTag = projectTag;
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
    
    public void setProjectTag(ProjectTag projectTag) {
        this.projectTag = projectTag;
    }

    @Enumerated(EnumType.STRING)
    private ProjectStatus projectStatus;

    public boolean isAlmostDone() { return this.projectStatus == ProjectStatus.ALMOST_DONE; }
    
    public void markAsDone() {
        this.projectStatus = ProjectStatus.DONE;
    }

    public void markAsAlmostDone() {
        this.projectStatus = ProjectStatus.ALMOST_DONE;
    }

    public void markAsInProcess() {
        this.projectStatus = ProjectStatus.IN_PROGRESS;
    }

    public void setProjectExpectedTime(Integer projectExpectedTime) {
        this.projectExpectedTime = projectExpectedTime;
    }

    private Integer projectProgressRate;
    
    public void setProjectProgressRate(Integer projectProgressRate) {
         this.projectProgressRate = projectProgressRate;
    }

    public void setProjectLevel(Integer projectLevel) {
        this.projectLevel = projectLevel;
    }
    
    public void setProjectCoin(Integer projectCoin) {
        this.projectCoin = projectCoin;
    }
}
