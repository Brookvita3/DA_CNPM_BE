package L03.CNPM.Music.DTOS;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UploadPlaylistDTO {
    private String name;

    private String description;

    private Boolean isPublic;

    // list songId (vi xet ten co the bi trung)
    private List<String> songs;
}
