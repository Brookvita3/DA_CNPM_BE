package L03.CNPM.Music.responses.playlist;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.responses.song.SongResponse;
import L03.CNPM.Music.responses.users.UserResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaylistDetailResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("cover_url")
    private String coverUrl;

    @JsonProperty("is_public")
    private Boolean isPublic;

    @JsonProperty("status")
    private String status;

    @JsonProperty("user")
    private UserResponse user;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("songs")
    private List<SongResponse> songs;

    public static PlaylistDetailResponse fromPlaylist(Playlist playlist, List<SongResponse> songs, User user) {
        return PlaylistDetailResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverUrl(playlist.getCoverUrl())
                .isPublic(playlist.getIsPublic())
                .user(UserResponse.fromUser(user))
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .songs(songs == null || songs.isEmpty()
                        ? Collections.emptyList()
                        : songs)
                .build();
    }
}
