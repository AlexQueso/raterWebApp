package rater.web.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rater.web.app.classes.Test;

public interface TestRepository extends JpaRepository<Test, Long> {
}
