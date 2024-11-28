package L03.CNPM.Music.repositories;

import L03.CNPM.Music.models.Album;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findById(Long id);

    Optional<Album> findByName(String name);
}