package L03.CNPM.Music.repositories;

import L03.CNPM.Music.models.Song;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {
    @Query("SELECT s FROM Song s WHERE (:keyword IS NULL OR s.name LIKE %:keyword%)")
    Page<Song> findAll(@Param("keyword") String keyword, Pageable pageable);

    Optional<Song> findById(Long songId);
}
