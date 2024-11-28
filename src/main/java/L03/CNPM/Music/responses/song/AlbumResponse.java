//package L03.CNPM.Music.responses.song;
//
//import L03.CNPM.Music.models.Album;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.*;
//
//import java.util.List;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class AlbumResponse {
//    @JsonProperty("id")
//    private Long id;
//
//    @JsonProperty("name")
//    private String name;
//
//    @JsonProperty("description")
//    private String description;
//
//    @JsonProperty("cover_image_url")
//    private String coverImageUrl;
//
//    @JsonProperty("release_date")
//    private String releaseDate;
//
//    @JsonProperty("genre_id")
//    private List<String> genreId;
//
//    @JsonProperty("status")
//    private Album.Status status;
//
//    @JsonProperty("create_at")
//    private String createdAt;
//
//    public static AlbumResponse fromAlbum(Album album) {
//        return AlbumResponse.builder()
//                .id(album.getId())
//                .name(album.getName())
//                .description(album.getDescription())
//                .coverImageUrl(album.getCoverUrl())
//                .releaseDate(album.getReleaseDate())
//                .status(album.getStatus())
//                .build();
//    }
//
//}
