package cz.tournament.control.repository;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Tournament;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Combined entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CombinedRepository extends JpaRepository<Combined, Long> {

    @Query("select combined from Combined combined where combined.user.login = ?#{principal.username}")
    Page<Combined> findByUserIsCurrentUser(Pageable pageable);

    @Query("select combined from Combined combined left join fetch combined.allParticipants where combined.id =:id")
    Combined findOneWithEagerRelationships(@Param("id") Long id);
    
    List<Combined> findByGroupsContains(Tournament tournament);
    
    Combined findByPlayoff(Tournament tournament);
    
    List<Combined> findByAllParticipantsContains(Participant participant);
}
