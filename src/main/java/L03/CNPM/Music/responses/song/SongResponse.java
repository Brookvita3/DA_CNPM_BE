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

    @JsonProperty("secure_url")
    private String secureUrl;

    @JsonProperty("status")
    private String status;

    @JsonProperty("public_image_id")
    private String publicImageId;

    public static SongResponse fromSong(Song song) {
        return SongResponse.builder()
                .id(song.getId().toString())
                .name(song.getName())
                .secureUrl(song.getSecureUrl())
                .publicImageId(song.getPublicImageId())
                .status(song.getStatus().name())
                .build();
    }
}
