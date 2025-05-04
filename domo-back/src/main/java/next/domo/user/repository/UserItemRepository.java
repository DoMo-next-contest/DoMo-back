package next.domo.user.repository;

import next.domo.user.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    @Query("SELECT ui.item.itemId FROM UserItem ui WHERE ui.user.userId = :userId")
    List<Long> findItemIdsByUserId(@Param("userId") Long userId);
}
