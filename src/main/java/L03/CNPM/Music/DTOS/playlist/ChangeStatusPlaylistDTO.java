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
public class ChangeStatusPlaylistDTO {
    @JsonProperty("playlist_id")
    private List<Long> playlist_id;
}
