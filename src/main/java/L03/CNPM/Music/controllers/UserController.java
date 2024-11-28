package L03.CNPM.Music.controllers;

import L03.CNPM.Music.DTOS.ResetPasswordDTO;
import L03.CNPM.Music.DTOS.UserDTO;
import L03.CNPM.Music.DTOS.UserLoginDTO;
import L03.CNPM.Music.components.LocalizationUtils;
import L03.CNPM.Music.components.SecurityUtils;
import L03.CNPM.Music.models.Token;
import L03.CNPM.Music.models.User;
import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.users.LoginResponse;
import L03.CNPM.Music.responses.users.UserListResponse;
import L03.CNPM.Music.responses.users.UserResponse;
import L03.CNPM.Music.services.token.ITokenService;
import L03.CNPM.Music.services.users.IUserService;
import L03.CNPM.Music.utils.MessageKeys;
import L03.CNPM.Music.utils.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
        private final LocalizationUtils localizationUtils;
        private final SecurityUtils securityUtils;
        private final IUserService userService;
        private final ITokenService tokenService;

        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @GetMapping("")
        public ResponseEntity<ResponseObject> getAllUser(
                        @RequestParam(defaultValue = "", required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int limit) {
                // Tạo Pageable từ thông tin trang và giới hạn
                PageRequest pageRequest = PageRequest.of(
                                page, limit,
                                // Sort.by("createdAt").descending()
                                Sort.by("id").ascending());
                Page<UserResponse> userPage = userService.findAll(keyword, pageRequest)
                                .map(UserResponse::fromUser);

                // Lấy tổng số trang
                int totalPages = userPage.getTotalPages();
                List<UserResponse> userResponses = userPage.getContent();
                UserListResponse userListResponse = UserListResponse
                                .builder()
                                .users(userResponses)
                                .totalPages(totalPages)
                                .build();

                return ResponseEntity.ok().body(ResponseObject.builder()
                                .message("Get user list successfully")
                                .status(HttpStatus.OK)
                                .data(userListResponse)
                                .build());
        }

        @PostMapping("/register")
        public ResponseEntity<ResponseObject> createUser(
                        @Valid @RequestBody UserDTO userDTO,
                        BindingResult result) throws Exception {
                if (result.hasErrors()) {
                        List<String> errorMessages = result.getFieldErrors()
                                        .stream()
                                        .map(FieldError::getDefaultMessage)
                                        .toList();

                        return ResponseEntity.badRequest().body(ResponseObject.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .message(errorMessages.toString())
                                        .build());
                }

                if (userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
                        return ResponseEntity.badRequest().body(ResponseObject.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .message("Email is required")
                                        .build());
                } else {
                        // Email not blank
                        if (!ValidationUtils.isValidEmail(userDTO.getEmail())) {
                                throw new Exception("Invalid email format");
                        }
                }

                if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                        // registerResponse.setMessage();
                        return ResponseEntity.badRequest().body(ResponseObject.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .data(null)
                                        .message(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH))
                                        .build());
                }

                User newUser = userService.createUser(userDTO);
                return ResponseEntity.ok(ResponseObject.builder()
                                .status(HttpStatus.CREATED)
                                .data(UserResponse.fromUser(newUser))
                                .message(MessageKeys.REGISTER_SUCCESSFULLY)
                                .build());
        }

        @PostMapping("/login")
        public ResponseEntity<ResponseObject> loginUser(
                        @Valid @RequestBody UserLoginDTO userLoginDTO,
                        HttpServletRequest request) throws Exception {
                // Check login information and generate token
                String token = userService.loginGetToken(userLoginDTO);
                String userAgent = request.getHeader("User-Agent");

                User userDetail = userService.getUserDetailsByExtractingToken(token);
                Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

                LoginResponse loginResponse = LoginResponse.builder()
                                .message("user.login.login_successfully")
                                .token(jwtToken.getToken())
                                .username(userDetail.getUsername())
                                .roles(userDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                                                .toList()) // method reference
                                .id(userDetail.getId())
                                .build();

                return ResponseEntity.ok().body(ResponseObject.builder()
                                .message(loginResponse.getMessage())
                                .data(loginResponse)
                                .status(HttpStatus.OK)
                                .build());
        }

        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        @PostMapping(value = "/upload-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ResponseObject> uploadProfileImageUser(
                        @RequestParam("file") MultipartFile file) throws Exception {
                User loginUser = securityUtils.getLoggedInUser();
                if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest().body(
                                        ResponseObject.builder()
                                                        .message(localizationUtils.getLocalizedMessage(
                                                                        MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE))
                                                        .build());
                }

                if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                        .body(ResponseObject.builder()
                                                        .message(localizationUtils.getLocalizedMessage(
                                                                        MessageKeys.UPLOAD_IMAGES_FILE_LARGE))
                                                        .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                                        .build());
                }

                // Check file type
                if (!isImageFile(file)) {
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                        .body(ResponseObject.builder()
                                                        .message("Uploaded file must be an image.")
                                                        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                                        .build());
                }

                String newUrl = userService.updateUserImageProfile(loginUser.getId(), file);
                return ResponseEntity.ok().body(ResponseObject.builder()
                                .message("Default success message")
                                .data(newUrl)
                                .status(HttpStatus.OK)
                                .build());
        }

        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST') or hasRole('ROLE_ADMIN')")
        @GetMapping("/details")
        public ResponseEntity<ResponseObject> getUserDetails(
                        @RequestHeader("Authorization") String authorizationHeader) throws Exception {
                String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
                User user = userService.getUserDetailsByExtractingToken(extractedToken);

                return ResponseEntity.ok().body(
                                ResponseObject.builder()
                                                .message("Get user's detail successfully")
                                                .data(UserResponse.fromUser(user))
                                                .status(HttpStatus.OK)
                                                .build());
        }

        @PreAuthorize("hasRole('ROLE_LISTENER') or hasRole('ROLE_ARTIST')")
        @PutMapping("/reset-password/{userId}")
        public ResponseEntity<ResponseObject> resetPassword(
                        @PathVariable Long userId,
                        @RequestBody ResetPasswordDTO resetPasswordDTO,
                        @RequestHeader("Authorization") String authorizationHeader) throws Exception {
                String extractedToken = authorizationHeader.substring(7);
                User user = userService.getUserDetailsByExtractingToken(extractedToken);

                // Ensure that the user making the request matches the user being updated
                if (!Objects.equals(user.getId(), userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }

                User updatedUser = userService.resetPassword(userId, resetPasswordDTO);
                return ResponseEntity.ok().body(
                                ResponseObject.builder()
                                                .message("Update user detail successfully")
                                                .data(UserResponse.fromUser(updatedUser))
                                                .status(HttpStatus.OK)
                                                .build());
        }

        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @PutMapping("/block/{userId}/{active}")
        public ResponseEntity<ResponseObject> blockOrEnable(
                        @Valid @PathVariable long userId,
                        @Valid @PathVariable int active) throws Exception {
                userService.blockOrEnable(userId, active > 0);
                String message = active > 0 ? "Successfully enabled the user." : "Successfully blocked the user.";
                return ResponseEntity.ok().body(ResponseObject.builder()
                                .message(message)
                                .status(HttpStatus.OK)
                                .data(null)
                                .build());
        }

        private boolean isMobileDevice(String userAgent) {
                return userAgent.toLowerCase().contains("mobile");
        }

        public static boolean isImageFile(MultipartFile file) {
                return true;
                /*
                 * String contentType = file.getContentType();
                 * return contentType != null && contentType.startsWith("image/");
                 */
                /*
                 * AutoDetectParser parser = new AutoDetectParser();
                 * Detector detector = parser.getDetector();
                 * try {
                 * Metadata metadata = new Metadata();
                 * TikaInputStream stream = TikaInputStream.get(file.getInputStream());
                 * MediaType mediaType = detector.detect(stream, metadata);
                 * String mimeType = mediaType.toString();
                 * } catch (IOException e) {
                 * return false;
                 * }
                 */
        }
}