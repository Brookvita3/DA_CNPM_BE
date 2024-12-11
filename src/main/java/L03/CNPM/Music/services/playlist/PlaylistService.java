package L03.CNPM.Music.services.playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import L03.CNPM.Music.DTOS.playlist.UploadSongToPlaylistDTO;
import L03.CNPM.Music.models.*;
import L03.CNPM.Music.responses.song.SongResponse;
import org.springframework.stereotype.Service;

import L03.CNPM.Music.DTOS.playlist.UploadPlaylistDTO;

import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.repositories.PlaylistRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.utils.DateUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaylistService implements IPlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final DateUtils dateUtils;

    @Override
    public Playlist Detail(Long playlistId) throws Exception {
        Optional<Playlist> playlist = playlistRepository.findById(playlistId);
        if (playlist.isEmpty()) {
            throw new DataNotFoundException("Playlist not found");
        }

        return playlist.get();
    }

    @Override
    public Playlist Create(UploadPlaylistDTO createPlaylistDTO, String userId) throws Exception {
        Optional<User> user = userRepository.findById(Long.parseLong(userId));
        if (user.isEmpty()) {
            throw new DataNotFoundException("User not found");
        }

        Playlist playlist = Playlist.builder()
                .name(createPlaylistDTO.getName())
                .description(createPlaylistDTO.getDescription())
                .coverUrl(createPlaylistDTO.getCoverUrl())
                .userId(user.get().getId())
                .isPublic(createPlaylistDTO.getIsPublic())
                .createdAt(dateUtils.getCurrentDate())
                .updatedAt(dateUtils.getCurrentDate())
                .build();

        return playlistRepository.save(playlist);
    }

    @Override
    public List<SongResponse> uploadSongToPlaylist(UploadSongToPlaylistDTO uploadSongToPlaylistDTO, Long playlistId)
            throws DataNotFoundException {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new DataNotFoundException("Playlist with ID %s no found".formatted(playlistId)));

        List<Song> songs = new ArrayList<>();
        for (Long songId : uploadSongToPlaylistDTO.getSongIds()) {
            Optional<Song> existedSong = songRepository.findById(songId);
            if (existedSong.isEmpty()) {
                throw new DataNotFoundException("Song with ID %s not found".formatted(songId));
            }
            Song song = existedSong.get();
            songs.add(song);
        }
        playlist.setSongs(songs);
        playlistRepository.save(playlist);
        return songs.stream().map(SongResponse::fromSong).toList();
    }
}
