//package L03.CNPM.Music.controllers;
//
//import L03.CNPM.Music.DTOS.UploadAlbumDTO;
//import L03.CNPM.Music.models.Album;
//import L03.CNPM.Music.responses.ResponseObject;
//import L03.CNPM.Music.responses.song.AlbumResponse;
//import L03.CNPM.Music.services.album.AlbumService;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import javax.crypto.SecretKey;
//
//@RestController
//@RequestMapping("${api.prefix}/album")
//@RequiredArgsConstructor
//        public class AlbumController {
//        private final AlbumService albumService;
//        @Value("${jwt.secretKey}")
//        private String SECRETKEY;
//
//        @GetMapping("/create")
//        public ResponseEntity<ResponseObject> uploadAlbum(
//                @RequestBody UploadAlbumDTO uploadAlbumDTO,
//                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
//        ) {
//                String token = authorizationHeader.substring(7);
//                SecretKey secretKey = Keys.hmacShaKeyFor(SECRETKEY.getBytes());
//                Long artistId = Jwts.parser()
//                        .verifyWith(secretKey)
//                        .build()
//                        .parseSignedClaims(token)
//                        .getPayload()
//                        .get("id", Long.class);
//
//                Album album = albumService.uploadAlbum(uploadAlbumDTO, artistId);
//
//                return ResponseEntity.ok().body(
//                        ResponseObject.builder()
//                                .message("upload album successfully")
//                                .data(AlbumResponse.fromAlbum(album))
//                                .build()
//                );
//
//        }
//}
