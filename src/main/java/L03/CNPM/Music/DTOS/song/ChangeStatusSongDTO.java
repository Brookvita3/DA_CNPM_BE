package L03.CNPM.Music.DTOS.song;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangeStatusSongDTO {
    @JsonProperty("song_id")
    private List<Long> song_id;
}
