package L03.CNPM.Music.controllers;

import L03.CNPM.Music.services.song.ISongService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import L03.CNPM.Music.DTOS.song.SongMetadataDTO;
import L03.CNPM.Music.components.JwtTokenUtils;
import L03.CNPM.Music.models.Song;
import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.song.CloudinaryResponse;
import L03.CNPM.Music.responses.song.SongCreateResponse;
import L03.CNPM.Music.responses.song.SongListResponse;
import L03.CNPM.Music.responses.song.SongResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/songs")
@RequiredArgsConstructor
public class SongController {
        private final ISongService songService;
        private final JwtTokenUtils jwtTokenUtils;

        @GetMapping("")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> getAllSong(
                        @RequestParam(defaultValue = "", required = false) String keyword,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int limit) {
                if (page < 1) {
                        page = 1;
                }

                PageRequest pageRequest = PageRequest.of(
                                page - 1, limit,
                                Sort.by("id").ascending());

                Page<SongResponse> songPage = songService.findAll(keyword, pageRequest)
                                .map(SongResponse::fromSong);

                int totalPages = songPage.getTotalPages();
                List<SongResponse> songResponses = songPage.getContent();
                SongListResponse songListResponse = SongListResponse.builder()
                                .songs(songResponses)
                                .totalPages(totalPages)
                                .build();

                return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                .message("Get all song successfully")
                                .status(HttpStatus.OK)
                                .data(songListResponse)
                                .build());
        }

        @PostMapping("/cloudinary")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> uploadSongToCloudinary(
                        @RequestPart MultipartFile file) throws Exception {
                try {
                        Map<String, Object> response = songService.uploadSong(file);

                        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                        .message("Upload song successfully")
                                        .status(HttpStatus.OK)
                                        .data(CloudinaryResponse.fromMap(response))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        @PostMapping("")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> uploadSong(
                        @Valid @RequestBody SongMetadataDTO metadataSongDTO,
                        BindingResult result) {
                if (result.hasErrors()) {
                        List<String> errorMessages = result.getFieldErrors()
                                        .stream()
                                        .map(FieldError::getDefaultMessage)
                                        .toList();

                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(errorMessages.toString())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }

                try {
                        Song newSong = songService.createSong(metadataSongDTO);

                        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                        .message("Create song successfully")
                                        .status(HttpStatus.OK)
                                        .data(SongCreateResponse.fromSong(newSong))
                                        .build());
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                                        .message(e.getMessage())
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .build());
                }
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ResponseObject> updateSong(@PathVariable String id,
                        @RequestHeader("Authorization") String authorizationHeader) {
                String token = authorizationHeader.substring(7);
                String userId = jwtTokenUtils.getSubject(token);

                if (userId == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseObject.builder()
                                        .message("Unauthorized")
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .data(null)
                                        .build());
                }

                try {
                        Song song = songService.updateSong(id, Long.valueOf(userId));

                        return ResponseEntity.status(HttpStatus.OK).body(ResponseObject.builder()
                                        .message("Update song successfully")
                                        .status(HttpStatus.OK)
                                        .data(SongCreateResponse.fromSong(song))
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
