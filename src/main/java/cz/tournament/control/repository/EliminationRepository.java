package cz.tournament.control.repository;

import cz.tournament.control.domain.tournaments.Elimination;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the Elimination entity.
 */
@SuppressWarnings("unused")
public interface EliminationRepository extends JpaRepository<Elimination,Long> {
    @Query("select elimination "
            + "from Elimination elimination inner join Tournament tournament on elimination.id = tournament.id "
            + "where tournament.user.login = ?#{principal.username}")
    Page<Elimination> findByUserIsCurrentUser(Pageable pageable);

}
