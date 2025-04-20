package next.domo.subtask.repository;

import next.domo.project.entity.Project;
import next.domo.subtask.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    List<SubTask> findAllByProject(Project project);

    @Query("SELECT SUM(s.subTaskExpectedTime) FROM SubTask s WHERE s.project.projectId = :projectId")
    Integer sumExpectedTimeByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(st) FROM SubTask st WHERE st.project.projectId = :projectId AND st.subTaskIsDone = true")
    Integer countDoneByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(st) FROM SubTask st WHERE st.project.projectId = :projectId")
    Integer countAllByProjectId(@Param("projectId") Long projectId);
}
