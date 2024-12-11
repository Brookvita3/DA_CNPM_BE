package L03.CNPM.Music.services.report;

import L03.CNPM.Music.DTOS.report.CreateReportDTO;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.Report;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.repositories.ReportRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final ReportRepository reportRepository;
    private final DateUtils dateUtils;
    private final SongRepository songRepository;


    @Override
    public Report uploadReport(CreateReportDTO createReportDTO, Long songId, Long userId) throws DataNotFoundException {
        Song song = songRepository.findById(songId).orElse(null);
        if (song == null) {
            throw new DataNotFoundException("Song not found");
        }
        int currentReports = song.getNumberOfReport();
        song.setNumberOfReport(currentReports + 1);
        Report report = Report.builder()
                .createdAt(dateUtils.getCurrentDate())
                .songId(songId)
                .userId(userId)
                .reason(createReportDTO.getReason())
                .build();
        return reportRepository.save(report);
    }
}