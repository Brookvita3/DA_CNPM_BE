package L03.CNPM.Music.services.playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import L03.CNPM.Music.DTOS.playlist.ChangeStatusPlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadSongToPlaylistDTO;
import L03.CNPM.Music.models.*;
import L03.CNPM.Music.responses.song.SongResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import L03.CNPM.Music.DTOS.playlist.UpdatePlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadPlaylistDTO;

import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.exceptions.UploadCloudinaryException;
import L03.CNPM.Music.repositories.PlaylistRepository;
import L03.CNPM.Music.repositories.SongPlaylistRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.utils.DateUtils;
import L03.CNPM.Music.utils.ImageFileUtils;
import L03.CNPM.Music.utils.MessageKeys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaylistService implements IPlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final DateUtils dateUtils;
    private final ImageFileUtils imageFileUtils;
    private final Cloudinary cloudinary;
    private final SongPlaylistRepository songPlaylistRepository;

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

    // @Override
    // public List<SongResponse> uploadSongToPlaylist(UploadSongToPlaylistDTO
    // uploadSongToPlaylistDTO, Long playlistId)
    // throws DataNotFoundException {

    // Playlist playlist = playlistRepository.findById(playlistId)
    // .orElseThrow(() -> new DataNotFoundException("Playlist with ID %s no
    // found".formatted(playlistId)));

    // List<Song> songs = new ArrayList<>();
    // for (Long songId : uploadSongToPlaylistDTO.getSongIds()) {
    // Optional<Song> existedSong = songRepository.findById(songId);
    // if (existedSong.isEmpty()) {
    // throw new DataNotFoundException("Song with ID %s not
    // found".formatted(songId));
    // }
    // Song song = existedSong.get();
    // songs.add(song);
    // }
    // playlist.setSongs(songs);
    // playlist.setStatus(Playlist.Status.PENDING);
    // playlistRepository.save(playlist);
    // return songs.stream().map(SongResponse::fromSong).toList();
    // }

    @Override
    public Playlist UploadImagePlaylist(MultipartFile file, Long playlistId) throws Exception {
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if (optionalPlaylist.isEmpty()) {
            throw new DataNotFoundException("album not found.");
        }
        Playlist existingPlaylist = optionalPlaylist.get();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File does not exist or is empty.");
        }

        String fileType = file.getContentType();
        if (!imageFileUtils.isValidImageFile(fileType, file.getOriginalFilename())) {
            throw new IllegalArgumentException("File type is not supported: " + fileType);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", "imageFolder");

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            existingPlaylist.setCoverUrl((String) uploadResult.get("url"));
            playlistRepository.save(existingPlaylist);
        } catch (Exception e) {
            throw new UploadCloudinaryException(MessageKeys.CLOUDINARY_UPLOAD_FAIL);
        }
        return existingPlaylist;
    }

    @Override
    @Transactional
    public Playlist updatePlaylist(UpdatePlaylistDTO updatePlaylistDTO, Long playlistId) throws DataNotFoundException {
        Optional<Playlist> exitedPlaylist = playlistRepository.findById(playlistId);
        if (exitedPlaylist.isEmpty())
            throw new DataNotFoundException("Playlist with ID %s no found".formatted(playlistId));
        Playlist playlist = exitedPlaylist.get();
        // if (playlist.getStatus() != Playlist.Status.DRAFT)
        // throw new DataNotFoundException("Playlist with ID %s is not approved
        // yet".formatted(playlistId));

        if (updatePlaylistDTO.getName() != null)
            playlist.setName(updatePlaylistDTO.getName());
        if (updatePlaylistDTO.getIsPublic() != null)
            playlist.setIsPublic(updatePlaylistDTO.getIsPublic());
        if (updatePlaylistDTO.getDescription() != null)
            playlist.setDescription(updatePlaylistDTO.getDescription());

        playlist.setUpdatedAt(dateUtils.getCurrentDate());

        verifyListSongId(updatePlaylistDTO.getAddList());
        verifyListSongId(updatePlaylistDTO.getDeleteList());

        Map<Long, Song> addListSongs = songRepository.findAllById(updatePlaylistDTO.getAddList())
                .stream()
                .collect(Collectors.toMap(Song::getId, song -> song));

        for (Map.Entry<Long, Song> entry : addListSongs.entrySet()) {
            Optional<SongPlaylist> exitedSongInPlaylist = songPlaylistRepository
                    .findBySongIdAndPlaylistId(entry.getKey(), playlistId);
            if (exitedSongInPlaylist.isPresent())
                throw new DataNotFoundException("song with ID %s is already in playlist".formatted(entry.getKey()));
            SongPlaylist songPlaylist = SongPlaylist.builder()
                    .song(entry.getValue())
                    .playlist(playlist)
                    .build();
            songPlaylistRepository.save(songPlaylist);
        }

        Map<Long, Song> deleteListSongs = songRepository.findAllById(updatePlaylistDTO.getDeleteList())
                .stream()
                .collect(Collectors.toMap(Song::getId, song -> song));

        for (Map.Entry<Long, Song> entry : deleteListSongs.entrySet()) {
            Optional<SongPlaylist> exitedSongInPlaylist = songPlaylistRepository
                    .findBySongIdAndPlaylistId(entry.getKey(), playlistId);
            if (exitedSongInPlaylist.isEmpty())
                throw new DataNotFoundException("song with ID %s is not in playlist yet".formatted(entry.getKey()));
            songPlaylistRepository.deleteSongPlaylistBySongIdAndPlaylistId(entry.getKey(), playlistId);
        }

        playlist.setStatus(Playlist.Status.PENDING);

        return playlistRepository.save(playlist);

    }

    @Override
    public List<Playlist> approvePlaylist(ChangeStatusPlaylistDTO changeStatusPlaylistDTO)
            throws DataNotFoundException {
        List<Playlist> playlistList = playlistRepository.findAllById(changeStatusPlaylistDTO.getPlaylist_id());
        for (Playlist playlist : playlistList) {
            playlist.setStatus(Playlist.Status.APPROVED);
            playlistRepository.save(playlist);
        }
        return playlistList;
    }

    @Override
    public List<Playlist> rejectPlaylist(ChangeStatusPlaylistDTO changeStatusPlaylistDTO) throws DataNotFoundException {
        List<Playlist> playlistList = playlistRepository.findAllById(changeStatusPlaylistDTO.getPlaylist_id());
        for (Playlist playlist : playlistList) {
            playlist.setStatus(Playlist.Status.REJECTED);
            playlistRepository.save(playlist);
        }
        return playlistList;
    }

    public void verifyListSongId(List<Long> songIdList) throws DataNotFoundException {
        for (Long songId : songIdList) {
            Optional<Song> existedSong = songRepository.findById(songId);
            if (existedSong.isEmpty()) {
                throw new DataNotFoundException("Song with ID %s not found".formatted(songId));
            }
        }
    }

    @Override
    public Page<Playlist> findAll(String keyword, Pageable pageable) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        return playlistRepository.findAll(keyword, pageable);
    }

    @Override
    public Page<Playlist> searchPlaylist(String keyword, Pageable pageable) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        return playlistRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }
}
