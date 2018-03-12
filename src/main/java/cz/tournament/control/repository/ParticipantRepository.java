package cz.tournament.control.repository;

import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.*;

/**
 * Spring Data JPA repository for the Participant entity.
 */
@SuppressWarnings("unused")
public interface ParticipantRepository extends JpaRepository<Participant,Long> {

    @Query("select participant from Participant participant where participant.user.login = ?#{principal.username}")
    List<Participant> findByUserIsCurrentUser();
    
    Participant findByPlayer(Player player);
    Participant findByTeam(Team team);
    
    @Query("select participant from Participant participant where participant.user.login = ?#{principal.username} and participant.player is null and participant.team is null")
    List<Participant> findByTeamIsNullAndPlayerIsNullAndUserIsCurrentUser();

}
