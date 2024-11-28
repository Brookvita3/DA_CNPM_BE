package L03.CNPM.Music.responses.song;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class SongListResponse {
    private List<SongResponse> songs;
    private int totalPages;
}
