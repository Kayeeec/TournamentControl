package cz.tournament.control.repository;

import cz.tournament.control.domain.Swiss;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Swiss entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SwissRepository extends JpaRepository<Swiss, Long> {
    @Query("select swiss "
            + "from Swiss swiss inner join Tournament tournament on swiss.id = tournament.id "
            + "where tournament.user.login = ?#{principal.username} "
            + "and tournament.inCombined = false")
    Page<Swiss> findByUserIsCurrentUserAndInCombinedFalse(Pageable pageable);
}
