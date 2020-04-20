package rater.web.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rater.web.app.classes.Project;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Project findById(long id);

    Project findByName(String name);

    List<Project> findAll();

}
