package L03.CNPM.Music.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Getter
@Setter
public class UploadSongDTO {

    private String songName;

    private List<String> genres;

    private String duration;

    private String description;


}
