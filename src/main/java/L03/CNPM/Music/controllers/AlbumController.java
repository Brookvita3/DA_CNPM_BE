package L03.CNPM.Music.controllers;

import L03.CNPM.Music.DTOS.album.ChangeStatusAlbumDTO;
import L03.CNPM.Music.DTOS.album.UpdateAlbumDTO;
import L03.CNPM.Music.DTOS.album.UploadSongToAlbumDTO;
import L03.CNPM.Music.DTOS.album.UploadAlbumDTO;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.Album;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.components.JwtTokenUtils;
import L03.CNPM.Music.repositories.AlbumRepository;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.album.AlbumDetailResponse;
import L03.CNPM.Music.responses.album.AlbumResponse;
import L03.CNPM.Music.responses.song.SongResponse;
import L03.CNPM.Music.responses.users.UserListResponse;
import L03.CNPM.Music.responses.users.UserResponse;
import L03.CNPM.Music.services.album.AlbumService;
import L03.CNPM.Music.services.users.UserService;
import L03.CNPM.Music.utils.TokenUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/albums")
@RequiredArgsConstructor
public class AlbumController {
        private final AlbumService albumService;
        private final JwtTokenUtils jwtTokenUtils;
        private final AlbumRepository albumRepository;
        private final UserRepository userRepository;
        private final SongRepository songRepository;
        private final UserService userService;
        private final TokenUtils tokenUtils;

        // For admin get all album
        @GetMapping("/admin/all")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> AdminGetAlbum(
                        @RequestParam(defaultValue = "", required = false) String keyword,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit) {
                PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("id").ascending());
                Page<Album> albumList = albumRepository.AdminfindAll(keyword, pageRequest);
                try {
                        List<AlbumDetailResponse> albumResponseList = albumList.getContent().stream().map((album) -> {
                                List<SongResponse> songs = album.getSongs().stream().map(SongResponse::fromSong)
                                                .toList();
                                User artist = album.getArtist();
                                return AlbumDetailResponse.fromAlbum(album, songs, artist);
                        }).toList();
                        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                        .message("Get all album by admin successfully")
                                        .status(HttpStatus.OK)
                                        .data(albumResponseList)
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());

                }
                // List<UserResponse> userResponses = userPage.getContent();
                // UserListResponse userListResponse = UserListResponse
                // .builder()
                // .users(userResponses)
                // .totalPages(totalPages)
                // .currentPage(currentPage)
                // .itemsPerPage(itemsPerPage)
                // .build();
        }

        // For user get all album
        @GetMapping("")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> UserGetAlbum(
                        @RequestParam(defaultValue = "", required = false) String keyword) {
                Page<Album> albumList = albumRepository.findAll(keyword, null);
                List<AlbumResponse> albumResponseList = albumList.getContent().stream().map(AlbumResponse::fromAlbum)
                                .toList();
                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get all album successfully")
                                .status(HttpStatus.OK)
                                .data(albumResponseList)
                                .build());
        }

        // artist upload album
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
                                                        .data(AlbumDetailResponse.fromAlbum(album, null, artist))
                                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }

        }

        // ENDPOINT: {{API_PREFIX}}/albums/{id}/songs [PATCH]
        // SUBMIT SONG TO ALBUM, USE AFTER UPLOAD ALBUM TO DB, CHANGE ALBUM STATUS TO
        // PENDING, WAIT FOR ADMIN APPROVE
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
                                                        .data(AlbumDetailResponse.fromAlbum(album, songResponseList,
                                                                        artist))
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
                        List<SongResponse> songResponseList = songRepository.findAllByAlbumId(albumId).stream()
                                        .map(SongResponse::fromSong).toList();
                        return ResponseEntity.ok().body(
                                        ResponseObject.builder()
                                                        .message("update album successfully")
                                                        .status(HttpStatus.OK)
                                                        .data(AlbumDetailResponse.fromAlbum(album, songResponseList,
                                                                        artist))
                                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        // admin approve list album
        @PatchMapping("/approve")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> approveAlbums(
                        @RequestBody ChangeStatusAlbumDTO changeStatusAlbumDTO) {
                try {
                        List<Album> albumList = albumService.approveAlbum(changeStatusAlbumDTO);
                        List<AlbumResponse> albumResponseList = albumList.stream().map(AlbumResponse::fromAlbum)
                                        .toList();
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

        // admin reject list album
        @PatchMapping("/reject")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> rejectAlbums(
                        @RequestBody ChangeStatusAlbumDTO changeStatusAlbumDTO) {
                try {
                        List<Album> albumList = albumService.rejectAlbum(changeStatusAlbumDTO);
                        List<AlbumResponse> albumResponseList = albumList.stream().map(AlbumResponse::fromAlbum)
                                        .toList();
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
                        List<SongResponse> songResponseList = songRepository.findAllByAlbumId(id).stream()
                                        .map(SongResponse::fromSong).toList();
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

        // artist get album
        @GetMapping("/artist")
        @PreAuthorize("hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> GetArtistAlbum(
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestHeader("Authorization") String authorizationHeader) {
                String token = authorizationHeader.substring(7);
                String userId = jwtTokenUtils.getUserId(token);

                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                                        .message("Unauthorized")
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .data(null)
                                        .build());
                }

                List<AlbumResponse> albumResponseList = albumRepository.findByArtistId(Long.valueOf(userId)).stream()
                                .map(AlbumResponse::fromAlbum).toList();

                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get album successfully")
                                .status(HttpStatus.OK)
                                .data(albumResponseList)
                                .build());
        }

        // get songs by search
        @GetMapping("/search")
        @PreAuthorize("hasRole('ROLE_ARTIST') or hasRole('ROLE_LISTENER') or hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> searchAlbum(
                        @RequestParam(defaultValue = "", required = true) String name,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit) {
                PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("id").ascending());
                Page<Album> albums = albumService.searchAlbum(name, pageRequest);
                List<AlbumResponse> albumResponseList = albums.getContent().stream().map(AlbumResponse::fromAlbum)
                                .toList();
                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get list album by search successfully")
                                .status(HttpStatus.OK)
                                .data(albumResponseList)
                                .build());
        }

}
