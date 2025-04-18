package next.domo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findByUserUserId(Long userId);
}
