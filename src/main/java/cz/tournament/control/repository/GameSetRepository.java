package cz.tournament.control.repository;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.domain.SetSettings;
import java.util.List;
import org.springframework.data.jpa.repository.*;

/**
 * Spring Data JPA repository for the GameSet entity.
 */
@SuppressWarnings("unused")
public interface GameSetRepository extends JpaRepository<GameSet,Long> {
    
    List<GameSet> findByGame(Game game);
    List<GameSet> findBySetSettings(SetSettings setSettings);
    
}
