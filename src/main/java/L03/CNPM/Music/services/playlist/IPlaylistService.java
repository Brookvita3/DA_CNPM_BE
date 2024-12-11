package L03.CNPM.Music.services.playlist;

import L03.CNPM.Music.DTOS.playlist.ChangeStatusPlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UpdatePlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadPlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadSongToPlaylistDTO;
import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.responses.song.SongResponse;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface IPlaylistService {

    Playlist Detail(Long playlistId) throws Exception;

    Playlist Create(UploadPlaylistDTO createPlaylistDTO, String userId) throws Exception;

    List<SongResponse> uploadSongToPlaylist(UploadSongToPlaylistDTO uploadSongToPlaylistDTO, Long playlistId)
            throws Exception;

    Playlist UploadImagePlaylist(MultipartFile file, Long playlistId) throws Exception;

    Playlist updatePlaylist(UpdatePlaylistDTO updatePlaylistDTO, Long playlistId) throws Exception;

    List<Playlist> approvePlaylist(ChangeStatusPlaylistDTO changeStatusPlaylistDTO) throws Exception;

    List<Playlist> rejectPlaylist(ChangeStatusPlaylistDTO changeStatusPlaylistDTO) throws Exception;
}
