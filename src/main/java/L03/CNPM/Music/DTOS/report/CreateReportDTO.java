package L03.CNPM.Music.DTOS.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Getter
@Setter
public class CreateReportDTO {
    @JsonProperty("song_id")
    private Long song_id;

    @JsonProperty("reason")
    private String reason;
}
