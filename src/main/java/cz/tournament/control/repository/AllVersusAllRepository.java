package cz.tournament.control.repository;

import cz.tournament.control.domain.AllVersusAll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the AllVersusAll entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AllVersusAllRepository extends JpaRepository<AllVersusAll, Long> {
    
    @Query("select allversusall "
            + "from AllVersusAll allversusall inner join Tournament tournament on allversusall.id = tournament.id "
            + "where tournament.user.login = ?#{principal.username} "
            + "and tournament.inCombined = false")
    Page<AllVersusAll> findByUserIsCurrentUserAndInCombinedFalse(Pageable pageable);

}
