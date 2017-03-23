package cz.tournament.control.repository;

import cz.tournament.control.domain.Game;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Game entity.
 */
@SuppressWarnings("unused")
public interface GameRepository extends JpaRepository<Game,Long> {

}
