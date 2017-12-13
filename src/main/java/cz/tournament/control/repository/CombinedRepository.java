package cz.tournament.control.repository;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Tournament;
import java.util.List;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Spring Data JPA repository for the Combined entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CombinedRepository extends JpaRepository<Combined, Long> {

    @Query("select combined from Combined combined where combined.user.login = ?#{principal.username}")
    Page<Combined> findByUserIsCurrentUser(Pageable pageable);
    
    List<Combined> findByGroupsContains(Tournament tournament);
    
    Combined findByPlayoff(Tournament tournament);
    
    List<Combined> findByAllParticipantsContains(Participant participant);
    

}
