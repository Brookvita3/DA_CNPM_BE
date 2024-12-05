package L03.CNPM.Music.services.song;

import java.util.List;
import java.util.Map;

import L03.CNPM.Music.DTOS.song.ChangeStatusSongDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import L03.CNPM.Music.DTOS.song.SongMetadataDTO;
import L03.CNPM.Music.models.Song;

public interface ISongService {
    Page<Song> findAll(String keyword, Pageable pageable);

    Page<Song> findAllPending(String keyword, Pageable pageable);

    Page<Song> findAllByArtistId(String artistId, Pageable pageable);

    List<Song> approveSong(ChangeStatusSongDTO changeStatusSongDTO) throws Exception;

    List<Song> rejectSong(ChangeStatusSongDTO changeStatusSongDTO) throws Exception;

    Map<String, Object> uploadSong(MultipartFile file) throws Exception;

    Song UploadImageSong(MultipartFile file, Long songId) throws Exception;

    Song createSong(SongMetadataDTO metadataSongDTO) throws Exception;

    void deleteSong(String publicId) throws Exception;

    Song updateSong(String id, String userId) throws Exception;

    Song findById(Long id) throws Exception;
}