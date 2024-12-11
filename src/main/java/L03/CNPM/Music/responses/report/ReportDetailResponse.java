package L03.CNPM.Music.responses.report;

import L03.CNPM.Music.models.Report;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.responses.song.SongResponse;
import L03.CNPM.Music.responses.users.UserResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportDetailResponse {
    @JsonProperty("reason")
    private String reason;

    @JsonProperty("create_at")
    private String create_at ;

    @JsonProperty("song")
    private SongResponse songResponse;

    @JsonProperty("user")
    private UserResponse user;

    public static ReportDetailResponse fromReport(Report report, Song song, User user) {
        return ReportDetailResponse.builder()
                .user(UserResponse.fromUser(user))
                .songResponse(SongResponse.fromSong(song))
                .create_at(report.getCreatedAt())
                .reason(report.getReason())
                .build();
    }
}
