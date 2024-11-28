package L03.CNPM.Music.responses.song;

import com.fasterxml.jackson.annotation.JsonProperty;

import L03.CNPM.Music.models.Song;

import java.time.Duration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongCreateResponse {
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

    public static SongCreateResponse fromSong(Song song) {
        return SongCreateResponse.builder()
                .id(song.getId().toString())
                .name(song.getName())
                .duration(song.getDuration())
                .publicId(song.getPublicId())
                .status(song.getStatus().name())
                .build();
    }
}
