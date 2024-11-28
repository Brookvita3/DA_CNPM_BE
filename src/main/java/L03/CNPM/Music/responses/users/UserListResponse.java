package L03.CNPM.Music.responses.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UserListResponse {
    private List<UserResponse> users;
    private int totalPages;
}
