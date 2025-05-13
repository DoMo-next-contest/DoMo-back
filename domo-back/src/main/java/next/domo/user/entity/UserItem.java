package next.domo.user.entity;

import jakarta.persistence.*;
import lombok.*;
import next.domo.file.entity.Item;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class UserItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;


}
