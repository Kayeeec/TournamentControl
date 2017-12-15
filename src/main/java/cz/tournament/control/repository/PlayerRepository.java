package cz.tournament.control.repository;

import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.User;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the Player entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("select player from Player player where player.user.login = ?#{principal.username}")
    Page<Player> findByUserIsCurrentUser(Pageable pageable);

    List<Player> findByUser(User user);
    
    List<Player> findByName(String name);

}
