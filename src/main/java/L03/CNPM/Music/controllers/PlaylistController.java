package L03.CNPM.Music.controllers;

import java.util.List;

import L03.CNPM.Music.DTOS.album.ChangeStatusAlbumDTO;
import L03.CNPM.Music.DTOS.playlist.ChangeStatusPlaylistDTO;
import L03.CNPM.Music.responses.album.AlbumResponse;
import L03.CNPM.Music.responses.playlist.PlaylistResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import L03.CNPM.Music.DTOS.album.UpdateAlbumDTO;
import L03.CNPM.Music.DTOS.playlist.UpdatePlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadPlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadSongToPlaylistDTO;
import L03.CNPM.Music.components.JwtTokenUtils;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.SongRepository;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.models.Album;
import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.album.AlbumDetailResponse;
import L03.CNPM.Music.responses.playlist.PlaylistDetailResponse;
import L03.CNPM.Music.responses.song.SongResponse;
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

    @GetMapping("/{playlistId}")
    @PreAuthorize("hasRole('ROLE_LISTENER')")
    public ResponseEntity<ResponseObject> Detail(@PathVariable Long playlistId) throws Exception {
        try {
            Playlist playlist = playlistService.Detail(playlistId);
            List<SongResponse> songResponseList = playlist.getSongs().stream().map(SongResponse::fromSong).toList();
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
    @PreAuthorize("hasRole('ROLE_LISTENER')")
    public ResponseEntity<ResponseObject> Create(
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
    @PreAuthorize("hasRole('ROLE_LISTENER')")
    public ResponseEntity<ResponseObject> uploadSongToplaylist(
            @RequestBody UploadSongToPlaylistDTO addSongAlbumDTO,
            @PathVariable Long playlistId,
            @RequestHeader("Authorization") String authorizationHeader) throws Exception {
        try {
            List<SongResponse> songResponseList = playlistService.uploadSongToPlaylist(addSongAlbumDTO, playlistId);
            String token = authorizationHeader.substring(7);
            String userId = jwtTokenUtils.getUserId(token);
            Playlist playlist = playlistService.Detail(playlistId);
            // Long artistId = tokenUtils.getIdFromToken(authorizationHeader.substring(7));
            User user = userRepository.findById(Long.valueOf(userId)).orElse(null);

            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("add song to playlist successfully")
                            .data(PlaylistDetailResponse.fromPlaylist(playlist, songResponseList, user))
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
    @PreAuthorize("hasRole('ROLE_LISTENER')")
    public ResponseEntity<ResponseObject> UploadImageAlbum(
            @RequestPart MultipartFile file,
            @PathVariable Long playlistId) throws Exception {
        try {
            Playlist playlist = playlistService.UploadImagePlaylist(file, playlistId);
            List<SongResponse> songResponseList = playlist.getSongs().stream().map(SongResponse::fromSong).toList();
            User user = userService.Detail(playlist.getUserId());

            return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                    .message("Upload image for album successfully")
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
    @PreAuthorize("hasRole('ROLE_LISTENER')")
    public ResponseEntity<ResponseObject> updatePlaylist(
            @RequestBody UpdatePlaylistDTO updatePlaylistDTO,
            @PathVariable Long playlistId,
            @RequestHeader("Authorization") String authorizationHeader) throws Exception {
        try {
            Playlist playlist = playlistService.updatePlaylist(updatePlaylistDTO, playlistId);
            String token = authorizationHeader.substring(7);
            String userId = jwtTokenUtils.getUserId(token);
            User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            List<SongResponse> songResponseList = playlist.getSongs().stream().map(SongResponse::fromSong).toList();
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("update album successfully")
                            .status(HttpStatus.OK)
                            .data(PlaylistDetailResponse.fromPlaylist(playlist, songResponseList, user))
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
    public ResponseEntity<ResponseObject> approveAlbums(
            @RequestBody ChangeStatusPlaylistDTO changeStatusPlaylistDTO,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            String userId = jwtTokenUtils.getUserId(token);
            User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            List<Playlist> playlistList = playlistService.approvePlaylist(changeStatusPlaylistDTO);

            List<PlaylistResponse> playlistResponseList = playlistList.stream()
                    .map(playlist -> {return PlaylistResponse.fromPlaylist(playlist, user);})
                    .toList();
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("update album successfully")
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
    public ResponseEntity<ResponseObject> rejectPlaylist(
            @RequestBody ChangeStatusPlaylistDTO changeStatusPlaylistDTO,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            String userId = jwtTokenUtils.getUserId(token);
            User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            List<Playlist> playlistList = playlistService.rejectPlaylist(changeStatusPlaylistDTO);

            List<PlaylistResponse> playlistResponseList = playlistList.stream()
                    .map(playlist -> {return PlaylistResponse.fromPlaylist(playlist, user);})
                    .toList();
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("update album successfully")
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


}
