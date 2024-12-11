package L03.CNPM.Music.DTOS.playlist;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlaylistDTO {
    @JsonProperty("description")
    private String description;

    @JsonProperty("is_public")
    private Boolean isPublic;

    @JsonProperty("list_add")
    private List<Long> addList;

    @JsonProperty("list_delete")
    private List<Long> deleteList;

    @JsonProperty("name")
    private String name;
}
