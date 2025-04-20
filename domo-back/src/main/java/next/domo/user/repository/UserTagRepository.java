package next.domo.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import next.domo.subtask.entity.SubTaskTag;
import next.domo.user.entity.UserTag;
import next.domo.user.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findByUserUserId(Long userId);
    Optional<UserTag> findByUserAndSubTaskTag(User user, SubTaskTag subTaskTag);
}
