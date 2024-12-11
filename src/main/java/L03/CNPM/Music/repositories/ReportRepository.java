package L03.CNPM.Music.repositories;

import L03.CNPM.Music.models.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByUserId(Long userId);

    List<Report> findAllBySongId(Long songId);
}