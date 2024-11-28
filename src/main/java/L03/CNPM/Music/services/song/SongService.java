package L03.CNPM.Music.services.song;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import L03.CNPM.Music.DTOS.song.SongMetadataDTO;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.GenreRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.utils.AudioFileUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SongService implements ISongService {
    private final AudioFileUtils audioFileUtils;
    private final Cloudinary cloudinary;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

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
    public Song createSong(SongMetadataDTO metadataSongDTO) throws Exception {
        // if (metadataSongDTO.getAlbumId() != null) {
        // Optional<Album> existingAlbum =
        // albumRepository.findByAlbumId(metadataSongDTO.getAlbumId());
        // if (existingAlbum.isEmpty()) {
        // throw new DataNotFoundException("Album not found.");
        // }
        // }

        Song newSong = Song.builder()
                .name(metadataSongDTO.getName())
                .description(metadataSongDTO.getDescription())
                .releaseDate(metadataSongDTO.getReleaseDate())
                .duration(metadataSongDTO.getDuration())
                .status(Song.Status.DRAFT)
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
        Optional<User> existingArtist = userRepository.findById(userId);
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
        return songRepository.save(song);
    }
}