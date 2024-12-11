package L03.CNPM.Music.models;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "song_id", nullable = false)
    private Long songId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "create_at", nullable = false)
    private String createdAt;

}
