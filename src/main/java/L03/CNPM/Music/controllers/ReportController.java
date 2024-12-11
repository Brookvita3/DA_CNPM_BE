package L03.CNPM.Music.controllers;

import L03.CNPM.Music.DTOS.report.CreateReportDTO;
import L03.CNPM.Music.models.Report;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.ReportRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.report.ReportDetailResponse;
import L03.CNPM.Music.responses.report.ReportResponse;
import L03.CNPM.Music.services.report.IReportService;
import L03.CNPM.Music.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/reports")
@RequiredArgsConstructor
public class ReportController {
    private final IReportService reportService;
    private final TokenUtils tokenUtils;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_LISTENER')")
    public ResponseEntity<ResponseObject> uploadReport(
            @RequestBody CreateReportDTO createReportDTO,
            @RequestHeader("Authorization") String authorizationHeader) throws Exception {
        Long userId = tokenUtils.getIdFromToken(authorizationHeader.substring(7));
        User user = userRepository.findById(userId).orElse(null);
        Song song = songRepository.findById(createReportDTO.getSong_id()).orElse(null);
        Report report = reportService.uploadReport(createReportDTO, createReportDTO.getSong_id(), userId);
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("upload report successfully")
                .status(HttpStatus.OK)
                .data(ReportDetailResponse.fromReport(report, song, user))
                .build());
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_LISTENER')")
    public ResponseEntity<ResponseObject> userGetReport(
            @RequestHeader("Authorization") String authorizationHeader) {
        Long userId = tokenUtils.getIdFromToken(authorizationHeader.substring(7));
        User user = userRepository.findById(userId).orElse(null);
        List<Report> reportList = reportRepository.findAllByUserId(userId);
        List<ReportResponse> reportResponseList = reportList.stream().map(report -> {
            Song song = songRepository.findById(report.getSongId()).get();
            return ReportResponse.fromReport(report, song);
        }).toList();
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("all report of this account")
                .status(HttpStatus.OK)
                .data(reportResponseList)
                .build());
    }

    @GetMapping("/admin/{songId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> adminGetReport(
            @PathVariable Long songId) {
        List<Report> reportList = reportRepository.findAllBySongId(songId);

        List<ReportDetailResponse> reportDetailResponseList = reportList.stream().map(report -> {
            Song song = songRepository.findById(report.getSongId()).get();
            User user = userRepository.findById(report.getUserId()).get();
            return ReportDetailResponse.fromReport(report, song, user);
        }).toList();
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message("all report of this song")
                .status(HttpStatus.OK)
                .data(reportDetailResponseList)
                .build());
    }
}
