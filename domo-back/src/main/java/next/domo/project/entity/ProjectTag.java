package next.domo.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProjectTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectTagId;

    private String projectTagName;

    private Long userId;
}
