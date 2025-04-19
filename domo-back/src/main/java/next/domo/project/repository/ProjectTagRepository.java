package next.domo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import next.domo.project.entity.ProjectTag;

import java.util.List;
import java.util.Optional;

public interface ProjectTagRepository extends JpaRepository<ProjectTag, Long> {
    boolean existsByProjectTagNameAndUserUserId(String projectTagName, Long userId);
    Optional<ProjectTag> findByProjectTagNameAndUserUserId(String projectTagName, Long userId);
    List<ProjectTag> findByUserUserId(Long userId);
}