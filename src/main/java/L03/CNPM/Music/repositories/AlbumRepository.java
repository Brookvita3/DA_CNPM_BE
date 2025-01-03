package L03.CNPM.Music.repositories;

import L03.CNPM.Music.models.Album;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    @SuppressWarnings("null")
    Optional<Album> findById(Long albumId);

    Optional<Album> findByName(String name);

    @Query("SELECT a FROM Album a WHERE (:keyword IS NULL OR a.name LIKE %:keyword%) AND a.status = 'APPROVED'")
    Page<Album> findAll(@Param("keyword") String keyword, Pageable pageable);

    @SuppressWarnings("null")
    boolean existsById(Long albumId);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = { "songs", "artist" })
    @Query("SELECT a FROM Album a WHERE (:keyword IS NULL OR a.name LIKE %:keyword%) AND a.status != 'DRAFT'")
    Page<Album> AdminfindAll(@Param("keyword") String keyword, Pageable pageable);

    List<Album> findByArtistId(Long artistId);

    @Query("SELECT a FROM Album a WHERE (a.name LIKE %:keyword%) AND a.status = 'APPROVED'")
    Page<Album> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}