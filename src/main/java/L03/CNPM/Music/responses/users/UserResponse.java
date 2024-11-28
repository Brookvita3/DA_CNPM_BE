package L03.CNPM.Music.responses.users;

import L03.CNPM.Music.models.Role;
import L03.CNPM.Music.models.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("country")
    private String country;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("role")
    private Role role;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .country(user.getCountry())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .build();
    }
}
