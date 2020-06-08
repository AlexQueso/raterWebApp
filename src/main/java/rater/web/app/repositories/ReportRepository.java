package rater.web.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rater.web.app.classes.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Report findById(long id);
}
