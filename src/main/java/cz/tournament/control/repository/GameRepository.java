package cz.tournament.control.repository;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Tournament;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Game entity.
 */
@SuppressWarnings("unused")
public interface GameRepository extends JpaRepository<Game,Long> {
    
    List<Game> findByTournament(Tournament tournament);
}
