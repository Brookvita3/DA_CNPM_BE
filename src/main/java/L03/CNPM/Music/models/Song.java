package L03.CNPM.Music.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", length = 255)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "artist_id", nullable = false, length = 255)
    private Long artistId;

    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "duration", nullable = false)
    private Double duration;

    @Column(name = "secure_url", nullable = false, length = 255)
    private String secureUrl;

    @Column(name = "public_id", nullable = false, length = 255)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_date", nullable = false)
    private String releaseDate;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;

    @Column(name = "public_image_id")
    private String publicImageId;

    @Column(name = "genre_id")
    private Long genreId;

    @ManyToMany(mappedBy = "songs")
    private List<Playlist> playlists;

    @Column(name = "number_of_report")
    @Builder.Default
    private Integer numberOfReport = 0;

    public enum Status {
        DRAFT,
        PENDING,
        APPROVED,
        REJECTED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false)
    private User artist;
}