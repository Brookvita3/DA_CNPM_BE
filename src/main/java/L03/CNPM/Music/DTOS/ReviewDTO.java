package L03.CNPM.Music.DTOS;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Getter
@Setter
public class ReviewDTO {

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("rating")
    @NotNull
    @Min(0)
    @Max(10)
    private int rating;

}
