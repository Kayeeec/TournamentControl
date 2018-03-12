package cz.tournament.control.repository;

import cz.tournament.control.domain.Elimination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data JPA repository for the Elimination entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EliminationRepository extends JpaRepository<Elimination, Long> {
    @Query("select elimination "
            + "from Elimination elimination inner join Tournament tournament on elimination.id = tournament.id "
            + "where tournament.user.login = ?#{principal.username} "
            + "and tournament.inCombined = false")
    Page<Elimination> findByUserIsCurrentUserAndInCombinedFalse(Pageable pageable);

}
