package L03.CNPM.Music.controllers;

import java.util.List;
import java.util.Optional;

import L03.CNPM.Music.DTOS.playlist.ChangeStatusPlaylistDTO;
import L03.CNPM.Music.repositories.PlaylistRepository;
import L03.CNPM.Music.responses.playlist.PlaylistResponse;
import L03.CNPM.Music.responses.song.SongResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import L03.CNPM.Music.DTOS.playlist.UpdatePlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadPlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadSongToPlaylistDTO;
import L03.CNPM.Music.components.JwtTokenUtils;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.playlist.PlaylistDetailResponse;
import L03.CNPM.Music.services.playlist.IPlaylistService;
import L03.CNPM.Music.services.users.IUserService;

@RestController
@RequestMapping("${api.prefix}/playlists")
@RequiredArgsConstructor
public class PlaylistController {
        private final IPlaylistService playlistService;
        private final IUserService userService;
        private final JwtTokenUtils jwtTokenUtils;
        private final UserRepository userRepository;
        private final PlaylistRepository playlistRepository;

        @GetMapping("/{playlistId}")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> Detail(@PathVariable Long playlistId) throws Exception {
                try {
                        Playlist playlist = playlistService.Detail(playlistId);
                        List<SongResponse> songResponseList = playlist.getSongs().stream().map(SongResponse::fromSong)
                                        .toList();
                        User user = userService.Detail(playlist.getUserId());

                        return ResponseEntity.ok().body(ResponseObject.builder()
                                        .message("Get playlist detail successfully")
                                        .status(HttpStatus.OK)
                                        .data(PlaylistDetailResponse.fromPlaylist(playlist, songResponseList, user))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        @PostMapping("")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> uploadAlbum(
                        @RequestBody UploadPlaylistDTO createPlaylistDTO,
                        @RequestHeader("Authorization") String authorizationHeader) throws Exception {

                try {
                        String token = authorizationHeader.substring(7);
                        User user = userService.GetUserDetailByToken(token);
                        Playlist playlist = playlistService.Create(createPlaylistDTO, user.getId().toString());

                        return ResponseEntity.ok().body(ResponseObject.builder()
                                        .message("Create playlist successfully")
                                        .status(HttpStatus.OK)
                                        .data(PlaylistDetailResponse.fromPlaylist(playlist, null, user))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        @PatchMapping("/{playlistId}")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> uploadSongToplaylist(
                        @RequestBody UploadSongToPlaylistDTO addSongAlbumDTO,
                        @PathVariable Long playlistId,
                        @RequestHeader("Authorization") String authorizationHeader) throws Exception {
                try {
                        List<SongResponse> songResponseList = playlistService.uploadSongToPlaylist(addSongAlbumDTO,
                                        playlistId);
                        String token = authorizationHeader.substring(7);
                        String userId = jwtTokenUtils.getUserId(token);
                        Playlist playlist = playlistService.Detail(playlistId);
                        // Long artistId = tokenUtils.getIdFromToken(authorizationHeader.substring(7));
                        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);

                        return ResponseEntity.ok().body(
                                        ResponseObject.builder()
                                                        .message("add song to playlist successfully")
                                                        .data(PlaylistDetailResponse.fromPlaylist(playlist,
                                                                        songResponseList, user))
                                                        .status(HttpStatus.OK)
                                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        @PostMapping("/upload-playlist-image/{playlistId}")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> UploadImageAlbum(
                        @RequestPart MultipartFile file,
                        @PathVariable Long playlistId) throws Exception {
                try {
                        Playlist playlist = playlistService.UploadImagePlaylist(file, playlistId);
                        List<SongResponse> songResponseList = playlist.getSongs().stream().map(SongResponse::fromSong)
                                        .toList();
                        User user = userService.Detail(playlist.getUserId());

                        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                        .message("Upload image for playlist successfully")
                                        .status(HttpStatus.OK)
                                        .data(PlaylistDetailResponse.fromPlaylist(playlist, songResponseList, user))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        @PatchMapping("/{playlistId}/update")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> updatePlaylist(
                        @RequestBody UpdatePlaylistDTO updatePlaylistDTO,
                        @PathVariable Long playlistId,
                        @RequestHeader("Authorization") String authorizationHeader) throws Exception {
                try {
                        Playlist playlist = playlistService.updatePlaylist(updatePlaylistDTO, playlistId);
                        String token = authorizationHeader.substring(7);
                        String userId = jwtTokenUtils.getUserId(token);
                        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
                        List<SongResponse> songResponseList = playlist.getSongs().stream().map(SongResponse::fromSong)
                                        .toList();
                        return ResponseEntity.ok().body(
                                        ResponseObject.builder()
                                                        .message("update playlist successfully")
                                                        .status(HttpStatus.OK)
                                                        .data(PlaylistDetailResponse.fromPlaylist(playlist,
                                                                        songResponseList, user))
                                                        .build());
                } catch (DataNotFoundException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        // admin approve list playlist
        @PatchMapping("/approve")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> approvePlaylists(
                        @RequestBody ChangeStatusPlaylistDTO changeStatusPlaylistDTO,
                        @RequestHeader("Authorization") String authorizationHeader) {
                try {
                        String token = authorizationHeader.substring(7);
                        String userId = jwtTokenUtils.getUserId(token);
                        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
                        List<Playlist> playlistList = playlistService.approvePlaylist(changeStatusPlaylistDTO);

                        List<PlaylistResponse> playlistResponseList = playlistList.stream()
                                        .map(playlist -> {
                                                return PlaylistResponse.fromPlaylist(playlist, user);
                                        })
                                        .toList();
                        return ResponseEntity.ok().body(
                                        ResponseObject.builder()
                                                        .message("update playlist successfully")
                                                        .status(HttpStatus.OK)
                                                        .data(playlistResponseList)
                                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());

                }
        }

        // admin reject list playlist
        @PatchMapping("/reject")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> rejectPlaylists(
                        @RequestBody ChangeStatusPlaylistDTO changeStatusPlaylistDTO,
                        @RequestHeader("Authorization") String authorizationHeader) {
                try {
                        String token = authorizationHeader.substring(7);
                        String userId = jwtTokenUtils.getUserId(token);
                        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
                        List<Playlist> playlistList = playlistService.rejectPlaylist(changeStatusPlaylistDTO);

                        List<PlaylistResponse> playlistResponseList = playlistList.stream()
                                        .map(playlist -> {
                                                return PlaylistResponse.fromPlaylist(playlist, user);
                                        })
                                        .toList();
                        return ResponseEntity.ok().body(
                                        ResponseObject.builder()
                                                        .message("update playlist successfully")
                                                        .status(HttpStatus.OK)
                                                        .data(playlistResponseList)
                                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        // For admin get all playlist
        @GetMapping("/admin/all")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> AdminGetPlaylist(
                        @RequestParam(defaultValue = "", required = false) String keyword) {
                List<Playlist> playlistList = playlistRepository.AdminfindAll(keyword);
                List<PlaylistResponse> playlistResponseList = playlistList.stream().map(playlist -> {
                        return PlaylistResponse.fromPlaylist(playlist, null);
                }).toList();
                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get all playlist by admin successfully")
                                .status(HttpStatus.OK)
                                .data(playlistResponseList)
                                .build());
        }

        // For user get all playlist
        @GetMapping("")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> UserGetPlaylist(
                        @RequestParam(defaultValue = "", required = false) String keyword) {
                Page<Playlist> playlists = playlistRepository.findAll(keyword, null);
                List<PlaylistResponse> playlistResponseList = playlists.getContent().stream()
                                .map(playlist -> {
                                        Optional<User> user = userRepository.findById(playlist.getUserId());
                                        return PlaylistResponse.fromPlaylist(playlist, user.get());
                                })
                                .toList();
                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get all playlist successfully")
                                .status(HttpStatus.OK)
                                .data(playlistResponseList)
                                .build());
        }

        // listener get playlist
        @GetMapping("/listener")
        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        public ResponseEntity<ResponseObject> GetListenerPlaylist(
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

                List<PlaylistResponse> playlistResponseList = playlistRepository.findByUserId(Long.valueOf(userId))
                                .stream()
                                .map(playlist -> {
                                        Optional<User> user = userRepository.findById(playlist.getUserId());
                                        return PlaylistResponse.fromPlaylist(playlist, user.get());
                                }).toList();

                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get own playlist successfully")
                                .status(HttpStatus.OK)
                                .data(playlistResponseList)
                                .build());
        }

        // get songs by search
        @GetMapping("/search")
        @PreAuthorize("hasRole('ROLE_ARTIST') or hasRole('ROLE_LISTENER') or hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> searchPlaylist(
                        @RequestParam(defaultValue = "", required = true) String name,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit) {
                PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("id").ascending());
                Page<Playlist> playlists = playlistService.searchPlaylist(name, pageRequest);
                List<PlaylistResponse> playlistDetailResponseList = playlists.getContent().stream()
                                .map((playlist) -> {
                                        User user = userRepository.findById(playlist.getId()).orElse(null);
                                        return PlaylistResponse.fromPlaylist(playlist, user);
                                }).toList();
                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get list playlist by search successfully")
                                .status(HttpStatus.OK)
                                .data(playlistDetailResponseList)
                                .build());
        }

}
