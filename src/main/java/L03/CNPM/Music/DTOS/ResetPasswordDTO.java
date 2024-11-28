package L03.CNPM.Music.DTOS;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetPasswordDTO {
    @JsonProperty("password")
    private String password;

    @JsonProperty("retype_password")
    private String retypePassword;
}
