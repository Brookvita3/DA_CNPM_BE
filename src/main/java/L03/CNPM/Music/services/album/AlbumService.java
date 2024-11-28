//package L03.CNPM.Music.services.album;
//
//import L03.CNPM.Music.DTOS.UploadAlbumDTO;
//import L03.CNPM.Music.models.Album;
//import L03.CNPM.Music.models.Genre;
//import L03.CNPM.Music.repositories.AlbumRepository;
//import L03.CNPM.Music.repositories.GenreRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AlbumService implements IAlbumService {
//    private final AlbumRepository albumRepository;
//    private final GenreRepository genreRepository;
//
//    @Override
//    public Album uploadAlbum(UploadAlbumDTO uploadAlbumDTO, Long artistId) {
//        List<Genre> genreList = genreRepository.findByNameIn(uploadAlbumDTO.getGenre_Id());
//        Album album = Album.builder()
//                .name(uploadAlbumDTO.getName())
//                .description(uploadAlbumDTO.getDescription())
//                .status(Album.Status.DRAFT)
//                .artistId(artistId)
//                .coverUrl(uploadAlbumDTO.getCover_image_url())
//                .releaseDate(uploadAlbumDTO.getRelease_date())
//                .createdAt(SimpleDateFormat.getDateInstance().format(new Date()))
//                .updatedAt(SimpleDateFormat.getDateInstance().format(new Date()))
//                .genres(genreList)
//                .build();
//        albumRepository.save(album);
//        return album;
//    }
//}