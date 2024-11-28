package L03.CNPM.Music.controllers;

import L03.CNPM.Music.responses.ResponseObject;
import L03.CNPM.Music.responses.users.UserListResponse;
import L03.CNPM.Music.responses.users.UserResponse;
import L03.CNPM.Music.services.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ResponseObject> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                //Sort.by("createdAt").descending()
                Sort.by("id").ascending()
        );
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/block/{userId}/{active}")
    public ResponseEntity<ResponseObject> blockOrEnable(
            @Valid @PathVariable long userId,
            @Valid @PathVariable int active
    ) throws Exception {
        userService.blockOrEnable(userId, active > 0);
        String message = active > 0 ? "Successfully enabled the user." : "Successfully blocked the user.";
        return ResponseEntity.ok().body(ResponseObject.builder()
                .message(message)
                .status(HttpStatus.OK)
                .data(null)
                .build());
    }

}


