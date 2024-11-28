package L03.CNPM.Music.responses.users;
import L03.CNPM.Music.models.Song;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaylistResponse {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("isPublic")
    private Boolean isPublic;

    @JsonProperty("songs")
    private List<Song> songs;

    @JsonProperty("creationDate")
    private Date creationDate;
}