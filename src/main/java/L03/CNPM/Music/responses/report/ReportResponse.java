package L03.CNPM.Music.responses.report;

import L03.CNPM.Music.models.Report;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.responses.song.SongResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportResponse {
    @JsonProperty("name")
    private String name;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("create_at")
    private String create_at ;

    @JsonProperty("song")
    private SongResponse songResponse;

    public static ReportResponse fromReport(Report report, Song song) {
        return ReportResponse.builder()
                .songResponse(SongResponse.fromSong(song))
                .create_at(report.getCreatedAt())
                .reason(report.getReason())
                .build();
    }
}