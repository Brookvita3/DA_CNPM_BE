package L03.CNPM.Music.repositories;

import L03.CNPM.Music.models.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    Optional<Playlist> findById(String id);

    Optional<Playlist> findByName(String name);
}
