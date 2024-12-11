package L03.CNPM.Music.services.report;

import L03.CNPM.Music.DTOS.report.CreateReportDTO;
import L03.CNPM.Music.models.Report;

public interface IReportService {
    Report uploadReport(CreateReportDTO createReportDTO, Long songId, Long userId) throws Exception;
}
