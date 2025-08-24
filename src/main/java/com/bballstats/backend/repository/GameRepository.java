package com.bballstats.backend.repository;

import com.bballstats.backend.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    Optional<Game> findById(Long id);

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    Page<Game> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam"})
    @Query("""
            select g from Game g
            where (:teamId is null or g.homeTeam.id = :teamId or g.awayTeam.id = :teamId)
              and (:from is null or g.dateTime >= :from)
              and (:to   is null or g.dateTime <= :to)
            """)
    Page<Game> search(@Param("teamId") Long teamId,
                      @Param("from") LocalDateTime from,
                      @Param("to") LocalDateTime to,
                      Pageable pageable);

    // ====== DODATO ZA METRIKE (koristi se u MetricsServiceImpl) ======
    @Query("""
           select g from Game g
           where (g.homeTeam.id = :teamId or g.awayTeam.id = :teamId)
             and (:season is null or g.season = :season)
           """)
    List<Game> findByTeamAndSeason(@Param("teamId") Long teamId,
                                   @Param("season") String season);
}
