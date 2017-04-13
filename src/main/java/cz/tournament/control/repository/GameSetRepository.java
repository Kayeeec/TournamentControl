package cz.tournament.control.repository;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the GameSet entity.
 */
@SuppressWarnings("unused")
public interface GameSetRepository extends JpaRepository<GameSet,Long> {
    
    List<GameSet> findByGame(Game game);
}
