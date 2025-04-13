package next.domo.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import next.domo.project.entity.ProjectTag;

import java.util.List;
import java.util.Optional;

public interface ProjectTagRepository extends JpaRepository<ProjectTag, Long> {
    boolean existsByProjectTagNameAndUserId(String projectTagName, Long userId);
    Optional<ProjectTag> findByProjectTagNameAndUserId(String projectTagName, Long userId);
    List<ProjectTag> findByUserId(Long userId);
}