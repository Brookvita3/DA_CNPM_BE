package L03.CNPM.Music.controllers;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import L03.CNPM.Music.DTOS.playlist.UploadPlaylistDTO;
import L03.CNPM.Music.DTOS.playlist.UploadSongToPlaylistDTO;
import L03.CNPM.Music.components.JwtTokenUtils;
import L03.CNPM.Music.exceptions.DataNotFoundException;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.repositories.UserRepository;
import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.responses.ResponseObject;
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

    // @PatchMapping("/{playlistId}")
    // @PreAuthorize("hasRole('ROLE_LISTENER')")
    // public ResponseEntity<ResponseObject> Update(
    // @PathVariable Long playlistId,
    // @RequestBody UpdatePlaylistDTO updatePlaylistDTO,
    // @RequestHeader("Authorization") String authorizationHeader,
    // BindingResult results) throws Exception {
    // if (results.hasErrors()) {
    // return ResponseEntity.badRequest().body(ResponseObject.builder()
    // .message("Invalid request")
    // .status(HttpStatus.BAD_REQUEST)
    // .data(null)
    // .build());
    // }

    // try {
    // String token = authorizationHeader.substring(7);
    // User user = userService.GetUserDetailByToken(token);

    // Playlist playlist = playlistService.Update(playlistId, updatePlaylistDTO);

    // return ResponseEntity.ok().body(ResponseObject.builder()
    // .message("Update playlist successfully")
    // .status(HttpStatus.OK)
    // .data(PlaylistDetailResponse.fromPlaylist(playlist, user))
    // .build());
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(ResponseObject.builder()
    // .message(e.getMessage())
    // .status(HttpStatus.BAD_REQUEST)
    // .data(null)
    // .build());
    // }
    // }

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
}
