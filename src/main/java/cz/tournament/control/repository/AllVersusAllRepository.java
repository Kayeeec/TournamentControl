package cz.tournament.control.repository;

import cz.tournament.control.domain.tournaments.AllVersusAll;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the AllVersusAll entity.
 */
@SuppressWarnings("unused")
public interface AllVersusAllRepository extends JpaRepository<AllVersusAll,Long> {
    @Query("select allversusall "
            + "from AllVersusAll allversusall inner join Tournament tournament on allversusall.id = tournament.id "
            + "where tournament.user.login = ?#{principal.username} "
            + "and tournament.inCombined = false")
    List<AllVersusAll> findByUserIsCurrentUserAndInCombinedFalse();
    
}
