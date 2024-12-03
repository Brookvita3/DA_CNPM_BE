package L03.CNPM.Music.controllers;

import L03.CNPM.Music.DTOS.album.ChangeStatusAlbumDTO;
import L03.CNPM.Music.DTOS.album.UpdateAlbumDTO;
import L03.CNPM.Music.DTOS.album.UploadSongToAlbumDTO;
import L03.CNPM.Music.DTOS.album.UploadAlbumDTO;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.Album;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.album.AlbumDetailResponse;
import L03.CNPM.Music.responses.album.AlbumResponse;
import L03.CNPM.Music.responses.song.SongResponse;
import L03.CNPM.Music.services.album.AlbumService;
import L03.CNPM.Music.services.users.UserService;
import L03.CNPM.Music.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/albums")
public class AlbumController {
        private final AlbumService albumService;
        private final UserRepository userRepository;
        private final SongRepository songRepository;
        private final UserService userService;
        private final TokenUtils tokenUtils;


        @PostMapping("")
        @PreAuthorize("hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> uploadAlbum(
                        @RequestBody UploadAlbumDTO uploadAlbumDTO,
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
                Long artistId = tokenUtils.getIdFromToken(authorizationHeader.substring(7));
                User artist = userRepository.findById(artistId).orElse(null);
                try {
                        Album album = albumService.uploadAlbum(uploadAlbumDTO, artistId);
                        return ResponseEntity.ok().body(
                                        ResponseObject.builder()
                                                        .message("upload album successfully")
                                                        .status(HttpStatus.OK)
                                                        .data(AlbumDetailResponse.fromAlbum(album, null, artist ))
                                                        .build());
                        }
                catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST)
                                .data(null)
                                .build());
                }

        }

        // ENDPOINT: {{API_PREFIX}}/albums/{id}/songs [PATCH]
        // SUBMIT SONG TO ALBUM, USE AFTER UPLOAD ALBUM TO DB, CHANGE ALBUM STATUS TO PENDING, WAIT FOR ADMIN APPROVE
        // HEADERS: AUTHENTICATION: YES (ONLY ARTIST CAN ACCESS)
        // PARAMS:
        // id: String
        @PatchMapping("/{albumId}/songs")
        @PreAuthorize("hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> uploadSongToAlbum(
                        @RequestBody UploadSongToAlbumDTO addSongAlbumDTO,
                        @PathVariable Long albumId,
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
                try {
                        List<SongResponse> songResponseList = albumService.uploadSongToAlbum(addSongAlbumDTO, albumId);
                        Album album = albumService.Detail(albumId);
                        Long artistId = tokenUtils.getIdFromToken(authorizationHeader.substring(7));
                        User artist = userRepository.findById(artistId).orElse(null);

                        return ResponseEntity.ok().body(
                                        ResponseObject.builder()
                                                        .message("add song to album successfully")
                                                        .status(HttpStatus.OK)
                                                        .data(AlbumDetailResponse.fromAlbum(album, songResponseList, artist))
                                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }


        // ENDPOINT: {{API_PREFIX}}/albums/{id}/update [PATCH]
        // MODIFY ALBUM, USE AFTER ALBUM IS APPROVED
        // HEADERS: AUTHENTICATION: YES (ONLY ARTIST CAN ACCESS)
        // PARAMS:
        // id: String
        @PatchMapping("/{albumId}/update")
        @PreAuthorize("hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> updateAlbum(
                @RequestBody UpdateAlbumDTO updateAlbumDTO,
                @PathVariable Long albumId,
                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
                try {
                        Album album = albumService.updateAlbum(updateAlbumDTO, albumId);
                        Long artistId = tokenUtils.getIdFromToken(authorizationHeader.substring(7));
                        User artist = userRepository.findById(artistId).orElse(null);
                        List<SongResponse> songResponseList = songRepository.findAllByAlbumId(albumId).stream().map(SongResponse::fromSong).toList();
                        return ResponseEntity.ok().body(
                                ResponseObject.builder()
                                        .message("update album successfully")
                                        .status(HttpStatus.OK)
                                        .data(AlbumDetailResponse.fromAlbum(album, songResponseList, artist))
                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST)
                                .data(null)
                                .build());
                }
        }


        @PatchMapping("/approve")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> approveAlbums(
                @RequestBody ChangeStatusAlbumDTO changeStatusAlbumDTO) {
                try {
                        List<Album> albumList = albumService.approveAlbum(changeStatusAlbumDTO);
                        List<AlbumResponse> albumResponseList = albumList.stream().map(AlbumResponse::fromAlbum).toList();
                        return ResponseEntity.ok().body(
                                ResponseObject.builder()
                                        .message("update album successfully")
                                        .status(HttpStatus.OK)
                                        .data(albumResponseList)
                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST)
                                .data(null)
                                .build());
                }
        }

        @PatchMapping("/reject")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> rejectAlbums(
                @RequestBody ChangeStatusAlbumDTO changeStatusAlbumDTO) {
                try {
                        List<Album> albumList = albumService.rejectAlbum(changeStatusAlbumDTO);
                        List<AlbumResponse> albumResponseList = albumList.stream().map(AlbumResponse::fromAlbum).toList();
                        return ResponseEntity.ok().body(
                                ResponseObject.builder()
                                        .message("update album successfully")
                                        .status(HttpStatus.OK)
                                        .data(albumResponseList)
                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST)
                                .data(null)
                                .build());
                }
        }

        @PostMapping("/upload-album-image/{id}")
        @PreAuthorize("hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> UploadImageAlbum(
                @RequestPart MultipartFile file,
                @PathVariable Long id) throws Exception {
                try {
                        Album album = albumService.UploadImageAlbum(file, id);
                        List<SongResponse> songResponseList = songRepository.findAllByAlbumId(id).stream().map(SongResponse::fromSong).toList();
                        User artist = userService.Detail(album.getArtistId());

                        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Upload image for album successfully")
                                .status(HttpStatus.OK)
                                .data(AlbumDetailResponse.fromAlbum(album, songResponseList, artist))
                                .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST)
                                .data(null)
                                .build());
                }
        }
}
