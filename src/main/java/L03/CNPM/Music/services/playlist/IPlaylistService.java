package L03.CNPM.Music.services.playlist;

import L03.CNPM.Music.DTOS.playlist.UploadPlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadSongToPlaylistDTO;
import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.responses.song.SongResponse;

import java.util.List;

public interface IPlaylistService {

    Playlist Detail(Long playlistId) throws Exception;

    Playlist Create(UploadPlaylistDTO createPlaylistDTO, String userId) throws Exception;

    List<SongResponse> uploadSongToPlaylist(UploadSongToPlaylistDTO uploadSongToPlaylistDTO, Long playlistId)
            throws Exception;
}
