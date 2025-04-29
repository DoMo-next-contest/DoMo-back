package next.domo.user.entity;

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
    @Enumerated(EnumType.STRING)
    private SubTaskTag subTaskTag;  // 기존 userTagName → 의미 명확하게 변경

    @Column(nullable = false)
    private float actualToExpectedRate;
}