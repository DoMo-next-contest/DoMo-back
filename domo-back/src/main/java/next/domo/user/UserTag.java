package next.domo.user;

import jakarta.persistence.*;
import lombok.*;
import next.domo.subtask.entity.SubTaskTag;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false)
    private SubTaskTag userTagName;

    @Column(nullable = false)
    private int actualToExpectedRate;
}