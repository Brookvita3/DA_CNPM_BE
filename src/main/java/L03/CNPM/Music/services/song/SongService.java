package L03.CNPM.Music.services.song;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import L03.CNPM.Music.DTOS.song.ChangeStatusSongDTO;
import L03.CNPM.Music.exceptions.UploadCloudinaryException;
import L03.CNPM.Music.utils.ImageFileUtils;
import L03.CNPM.Music.utils.MessageKeys;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import L03.CNPM.Music.DTOS.song.SongMetadataDTO;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.Album;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.AlbumRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.utils.AudioFileUtils;
import L03.CNPM.Music.utils.DateUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongService implements ISongService {
    private final AudioFileUtils audioFileUtils;
    private final ImageFileUtils imageFileUtils;
    private final Cloudinary cloudinary;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final DateUtils dateUtils;

    @Override
    public Page<Song> findAll(String keyword, Pageable pageable) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        return songRepository.findAll(keyword, pageable);
    }

    @Override
    public Page<Song> findAllPending(String keyword, Pageable pageable) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        return songRepository.findAllPending(keyword, pageable);
    }

    @Override
    public Page<Song> findAllByArtistId(String artistId, Pageable pageable) {
        return songRepository.findAllByArtistId(Long.parseLong(artistId), pageable);
    }

    @Override
    public List<Song> approveSong(ChangeStatusSongDTO changeStatusSongDTO) throws Exception {
        List<Song> songList = songRepository.findAllById(changeStatusSongDTO.getSong_id());
        for (Song song : songList) {
            song.setStatus(Song.Status.APPROVED);
            songRepository.save(song);
        }
        return songList;
    }

    @Override
    public List<Song> rejectSong(ChangeStatusSongDTO changeStatusSongDTO) throws Exception {
        List<Song> songList = songRepository.findAllById(changeStatusSongDTO.getSong_id());
        for (Song song : songList) {
            song.setStatus(Song.Status.REJECTED);
            songRepository.save(song);
        }
        return songList;
    }

    @Override
    public Map<String, Object> uploadSong(MultipartFile file) throws Exception {
        Map<String, Object> response = null;

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File does not exist or is empty.");
        }

        String fileType = file.getContentType();
        if (!audioFileUtils.isValidAudioFile(fileType, file.getOriginalFilename())) {
            throw new IllegalArgumentException("File type is not supported: " + fileType);
        }

        File tempFile = audioFileUtils.convertMultipartFileToFile(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "resource_type", "video",
                    "folder", "songs");

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary
                    .uploader().upload(tempFile, uploadParams);

            response = Map.of(
                    "secure_url", uploadResult.get("secure_url"),
                    "public_id", uploadResult.get("public_id"),
                    "duration", uploadResult.get("duration"));
        } finally {
            tempFile.delete();
        }
        return response;
    }

    @Override
    public Song UploadImageSong(MultipartFile file, Long songId) throws Exception {
        Optional<Song> optionalSong = songRepository.findById(songId);
        if (optionalSong.isEmpty()) {
            throw new DataNotFoundException("song not found.");
        }
        Song existingSong = optionalSong.get();

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
            existingSong.setPublicImageId((String) uploadResult.get("url"));
            songRepository.save(existingSong);
        } catch (Exception e) {
            throw new UploadCloudinaryException(MessageKeys.CLOUDINARY_UPLOAD_FAIL);
        }
        return existingSong;
    }

    @Override
    public Song createSong(SongMetadataDTO metadataSongDTO) throws Exception {
        if (metadataSongDTO.getAlbumId() != null) {
            Optional<Album> existingAlbum = albumRepository.findById(metadataSongDTO.getAlbumId());
            if (existingAlbum.isEmpty()) {
                throw new DataNotFoundException("Album not found.");
            }
        }

        Song newSong = Song.builder()
                .name(metadataSongDTO.getName())
                .description(metadataSongDTO.getDescription())
                .releaseDate(metadataSongDTO.getReleaseDate())
                .artistId(metadataSongDTO.getArtistId())
                .albumId(metadataSongDTO.getAlbumId())
                .duration(metadataSongDTO.getDuration())
                .publicId(metadataSongDTO.getPublicId())
                .secureUrl(metadataSongDTO.getSecureUrl())
                .status(Song.Status.DRAFT)
                .createdAt(dateUtils.getCurrentDate())
                .updatedAt(dateUtils.getCurrentDate())
                .genreId(metadataSongDTO.getGenreId())
                .build();

        return songRepository.save(newSong);
    }

    @Override
    @Transactional
    public void deleteSong(String publicId) throws Exception {
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("Public ID is invalid.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                "resource_type", "video"));

        String result = (String) deleteResult.get("result");
        if (!"ok".equals(result)) {
            throw new RuntimeException("Delete song failed. Public ID: " + publicId);
        }
    }

    @Override
    public Song updateSong(String id, String userId) throws Exception {
        User user = null;
        Optional<User> existingArtist = userRepository.findById(Long.parseLong(userId));
        if (existingArtist.isEmpty()) {
            throw new DataNotFoundException("Artist not found.");
        }
        user = existingArtist.get();

        Optional<Song> existingSong = songRepository.findById(Long.parseLong(id));
        if (existingSong.isEmpty()) {
            throw new DataNotFoundException("Song not found.");
        }
        Song song = existingSong.get();

        if (!user.getId().equals(song.getArtistId())) {
            throw new DataNotFoundException("You are not the owner of this song.");
        }

        song.setStatus(Song.Status.PENDING);

        if (song.getCreatedAt() == null) {
            song.setCreatedAt(dateUtils.getCurrentDate());
        }
        song.setUpdatedAt(dateUtils.getCurrentDate());

        return songRepository.save(song);
    }

    @Override
    public Song findById(Long id) throws Exception {
        Optional<Song> song = songRepository.findById(id);
        if (song.isEmpty()) {
            throw new DataNotFoundException("Song not found.");
        }
        return song.get();
    }

    @Override
    public Page<Song> searchSong(String keyword, Pageable pageable) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        return songRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

}