package L03.CNPM.Music.DTOS.album;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangeStatusAlbumDTO {
    @JsonProperty("album_id")
    private List<Long> album_id;
}
