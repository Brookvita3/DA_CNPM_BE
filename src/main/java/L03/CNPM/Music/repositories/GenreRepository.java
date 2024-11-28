package L03.CNPM.Music.repositories;

import L03.CNPM.Music.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findById(Long id);

    List<Genre> findByNameIn(List<String> genreNames);
}
