package L03.CNPM.Music.repositories;

import L03.CNPM.Music.models.Album;
import L03.CNPM.Music.models.Playlist;
import L03.CNPM.Music.models.Song;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    @SuppressWarnings("null")
    Optional<Playlist> findById(Long id);

    Optional<Playlist> findByName(String name);

    @Query("SELECT p FROM Playlist p WHERE (:keyword IS NULL OR p.name LIKE %:keyword%) AND p.status != 'DRAFT'")
    List<Playlist> AdminfindAll(@Param("keyword") String keyword);

    @Query("SELECT p FROM Playlist p WHERE (:keyword IS NULL OR p.name LIKE %:keyword%) AND p.isPublic = true AND p.status = 'APPROVED'")
    Page<Playlist> findAll(@Param("keyword") String keyword, Pageable pageable);

    List<Playlist> findByUserId(Long userId);

    @Query("SELECT p FROM Playlist p WHERE (p.name LIKE %:keyword%) AND p.status = 'APPROVED' AND p.isPublic = true")
    Page<Playlist> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
