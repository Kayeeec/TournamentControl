package cz.tournament.control.repository;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Tournament;
import java.util.List;
import org.springframework.data.jpa.repository.*;

/**
 * Spring Data JPA repository for the Game entity.
 */
@SuppressWarnings("unused")
public interface GameRepository extends JpaRepository<Game,Long> {
    
    List<Game> findByTournament(Tournament tournament);
}
