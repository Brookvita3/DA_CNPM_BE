package L03.CNPM.Music.services.album;

import L03.CNPM.Music.DTOS.album.ChangeStatusAlbumDTO;
import L03.CNPM.Music.DTOS.album.UpdateAlbumDTO;
import L03.CNPM.Music.DTOS.album.UploadAlbumDTO;
import L03.CNPM.Music.DTOS.album.UploadSongToAlbumDTO;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.exceptions.UploadCloudinaryException;
import L03.CNPM.Music.models.Album;
import L03.CNPM.Music.models.Genre;
import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.repositories.AlbumRepository;
import L03.CNPM.Music.repositories.GenreRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.responses.song.SongResponse;
import L03.CNPM.Music.utils.DateUtils;
import L03.CNPM.Music.utils.ImageFileUtils;
import L03.CNPM.Music.utils.MessageKeys;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService implements IAlbumService {
    private final Cloudinary cloudinary;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final DateUtils dateUtils;
    private final GenreRepository genreRepository;
    private final ImageFileUtils imageFileUtils;

    @Override
    public Album uploadAlbum(UploadAlbumDTO uploadAlbumDTO, Long artistId) throws DataNotFoundException {
        List<Long> genre_id = (uploadAlbumDTO.getGenre_Id()).stream().map(Long::valueOf).toList();
        List<Genre> genreList = genreRepository.findGenresByIdIn(genre_id);
        if (albumRepository.existsByName(uploadAlbumDTO.getName())) {
            throw new DataNotFoundException("Album already exist");
        }
        Album album = Album.builder()
                .name(uploadAlbumDTO.getName())
                .description(uploadAlbumDTO.getDescription())
                .status(Album.Status.DRAFT)
                .artistId(artistId)
                .coverUrl(uploadAlbumDTO.getCoverImageUrl())
                .releaseDate(uploadAlbumDTO.getReleaseDate())
                .createdAt(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                .updatedAt(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                .genres(genreList)
                .build();
        albumRepository.save(album);
        return album;
    }

    @Override
    public List<SongResponse> uploadSongToAlbum(UploadSongToAlbumDTO uploadSongToAlbumDTO, Long albumId)
            throws DataNotFoundException {

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException("Album with ID %s no found".formatted(albumId)));
        album.setStatus(Album.Status.PENDING);
        albumRepository.save(album);

        List<Song> songs = new ArrayList<>();
        for (Long songId : uploadSongToAlbumDTO.getSongIds()) {
            Optional<Song> existedSong = songRepository.findById(songId);
            if (existedSong.isEmpty()) {
                throw new DataNotFoundException("Song with ID %s not found".formatted(songId));
            }
            Song song = existedSong.get();
            songs.add(song);
            song.setAlbumId(albumId);
            songRepository.save(song);
        }

        return songs.stream().map(SongResponse::fromSong).toList();
    }

    @Override
    public Page<Album> findAll(String keyword, Pageable pageable) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        return albumRepository.findAll(keyword, pageable);
    }

    @Override
    public Album updateAlbum(UpdateAlbumDTO updateAlbumDTO, Long albumId) throws DataNotFoundException {
        Optional<Album> exitedAlbum = albumRepository.findById(albumId);
        if (exitedAlbum.isEmpty())
            throw new DataNotFoundException("Album with ID %s no found".formatted(albumId));
        Album album = exitedAlbum.get();
        if (album.getStatus() != Album.Status.APPROVED)
            throw new DataNotFoundException("Album with ID %s is not approved yet".formatted(albumId));

        if (updateAlbumDTO.getName() != null)
            album.setName(updateAlbumDTO.getName());
        if (updateAlbumDTO.getDescription() != null)
            album.setDescription(updateAlbumDTO.getDescription());

        album.setUpdatedAt(dateUtils.getCurrentDate());

        verifyListSongId(updateAlbumDTO.getAddList());
        verifyListSongId(updateAlbumDTO.getDeleteList());

        Map<Long, Song> addListSongs = songRepository.findAllById(updateAlbumDTO.getAddList())
                .stream()
                .collect(Collectors.toMap(Song::getId, song -> song));

        for (Long songId : updateAlbumDTO.getAddList()) {
            Song song = addListSongs.get(songId);
            if (song != null) {
                song.setAlbumId(albumId);
                songRepository.save(song);
            }
        }

        Map<Long, Song> deleteListSongs = songRepository.findAllById(updateAlbumDTO.getDeleteList())
                .stream()
                .collect(Collectors.toMap(Song::getId, song -> song));

        for (Long songId : updateAlbumDTO.getDeleteList()) {
            Song song = deleteListSongs.get(songId);
            if (song != null) {
                song.setAlbumId(null);
                songRepository.save(song);
            }
        }
        return album;

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
    public Album Detail(Long albumId) throws DataNotFoundException {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new DataNotFoundException("Album with ID %s no found".formatted(albumId)));
    }

    @Override
    public List<Album> approveAlbum(ChangeStatusAlbumDTO changeStatusAlbumDTO) throws DataNotFoundException {
        List<Album> albumList = albumRepository.findAllById(changeStatusAlbumDTO.getAlbum_id());
        for (Album album : albumList) {
            album.setStatus(Album.Status.APPROVED);
            albumRepository.save(album);
        }
        return albumList;
    }

    @Override
    public List<Album> rejectAlbum(ChangeStatusAlbumDTO changeStatusAlbumDTO) throws DataNotFoundException {
        List<Album> albumList = albumRepository.findAllById(changeStatusAlbumDTO.getAlbum_id());
        for (Album album : albumList) {
            album.setStatus(Album.Status.REJECTED);
            albumRepository.save(album);
        }
        return albumList;
    }

    @Override
    public Album UploadImageAlbum(MultipartFile file, Long albumId)
            throws DataNotFoundException, UploadCloudinaryException {
        Optional<Album> optionalAlbum = albumRepository.findById(albumId);
        if (optionalAlbum.isEmpty()) {
            throw new DataNotFoundException("album not found.");
        }
        Album existingAlbum = optionalAlbum.get();

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
            existingAlbum.setCoverUrl((String) uploadResult.get("url"));
            albumRepository.save(existingAlbum);
        } catch (Exception e) {
            throw new UploadCloudinaryException(MessageKeys.CLOUDINARY_UPLOAD_FAIL);
        }
        return existingAlbum;
    }

    @Override
    public Page<Album> searchAlbum(String keyword, Pageable pageable) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }
        return albumRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }
}