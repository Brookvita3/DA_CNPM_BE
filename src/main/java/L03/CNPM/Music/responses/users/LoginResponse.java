package L03.CNPM.Music.responses.users;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String message;

    private String token;

    private String refreshToken;

    private String tokenType = "Bearer";

    private Long id;

    private String username;

    private List<String> roles;
}