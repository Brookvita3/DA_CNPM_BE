package L03.CNPM.Music.responses.song;

import com.fasterxml.jackson.annotation.JsonProperty;

import L03.CNPM.Music.models.Song;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SongResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("duration")
    private Double duration;

    @JsonProperty("public_id")
    private String publicId;

    @JsonProperty("status")
    private String status;

    public static SongResponse fromSong(Song song) {
        return SongResponse.builder()
                .id(song.getId().toString())
                .name(song.getName())
                .duration(song.getDuration())
                .publicId(song.getPublicId())
                .status(song.getStatus().name())
                .build();
    }
}
