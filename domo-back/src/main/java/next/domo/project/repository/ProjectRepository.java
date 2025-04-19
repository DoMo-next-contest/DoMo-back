package next.domo.project.repository;

import next.domo.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectTagId(Long projectTagId);
    List<Project> findByUserUserId(Long userId);
    Optional<Project> findTopByUserUserIdOrderByProjectIdDesc(Long userId);
}