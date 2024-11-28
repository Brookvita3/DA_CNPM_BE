package L03.CNPM.Music.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    @JsonProperty("email")
    private String email = "";

    @JsonProperty("username")
    private String username = "";

    @NotBlank(message = "Password cannot be blank")
    private String password = "";

    @JsonProperty("retype_password")
    private String retypePassword = "";

    @JsonProperty("country")
    private String country = "";

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @NotNull(message = "Role ID is required")
    @JsonProperty("role_id")
    // role admin not permitted
    private Long roleId;
}
