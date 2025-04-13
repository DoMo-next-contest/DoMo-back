package next.domo.subtask.repository;

import next.domo.project.entity.Project;
import next.domo.subtask.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    List<SubTask> findAllByProject(Project project);
}
