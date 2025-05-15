package next.domo.project.repository;

import next.domo.project.entity.Project;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectTag_ProjectTagId(Long projectTagId);
    List<Project> findByUserUserId(Long userId);
    List<Project> findByProjectTag_ProjectTagIdIn(List<Long> tagIds, Sort sort);
    List<Project> findAll(Sort sort);

    @Query("SELECT p FROM Project p WHERE p.user.userId = :userId AND p.projectStatus <> 'DONE' ORDER BY p.lastAccessedAt DESC")
    Optional<Project> findTopByUserIdAndNotDoneOrderByLastAccessedAtDesc(@Param("userId") Long userId);
}