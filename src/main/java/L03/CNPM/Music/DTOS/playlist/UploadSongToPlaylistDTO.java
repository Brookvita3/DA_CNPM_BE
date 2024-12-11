package L03.CNPM.Music.DTOS.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UploadSongToPlaylistDTO {
    @JsonProperty("song_ids")
    private List<Long> songIds;
}
