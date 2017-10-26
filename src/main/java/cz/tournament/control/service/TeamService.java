package cz.tournament.control.service;

import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.domain.exceptions.ParticipantInTournamentException;
import cz.tournament.control.repository.ParticipantRepository;
import cz.tournament.control.repository.TeamRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Karolina Bozkova
 */
@Service
@Transactional
public class TeamService {
    
    private final Logger log = LoggerFactory.getLogger(TeamService.class);

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ParticipantRepository participantRepository;
    private final TournamentService tournamentService;

    public TeamService(UserRepository userRepository, TeamRepository teamRepository, ParticipantRepository participantRepository, TournamentService tournamentService) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.participantRepository = participantRepository;
        this.tournamentService = tournamentService;
    }
    
    
    public Team createTeam(Team team){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        team.setUser(creator);
        
        Team result = teamRepository.save(team);
        
        Participant participant = participantRepository.save(new Participant(result, creator));
        
        log.debug("TEAM_SERVICE: Created Team: {}", result);
        log.debug("TEAM_SERVICE: Created Participant: {}", participant);
        
        return result;
    }
    
    public Team updateTeam(Team team){
        Team result = teamRepository.save(team);
        log.debug("TEAM_SERVICE: Updated Team: {}", result);
        return result;
    }
    
    @Transactional(readOnly = true)
    public List<Team> findAll(){
        List<Team> teams = teamRepository.findByUserIsCurrentUser();
        return teams;
    }
    
    @Transactional(readOnly = true)
    public Team findOne(Long id){
        Team team = teamRepository.findOneWithEagerRelationships(id);
        return team;
    }
    
    @Transactional(readOnly = true)
    public List<Tournament> findAllTournaments(Team team){
        Participant participant = participantRepository.findByTeam(team);
        return tournamentService.findByParticipant(participant);
    }
    
    /**
     * Deletes a team entity and its associated participant entity.
     * 
     * @param id of entity to delete
     * @throws cz.tournament.control.domain.exceptions.ParticipantInTournamentException
     */
    public void delete(Long id) throws ParticipantInTournamentException{
        Participant participant = participantRepository.findByTeam(teamRepository.findOne(id));
        List<Tournament> hisTournaments = tournamentService.findByParticipant(participant);
        if(!hisTournaments.isEmpty()){
            throw new ParticipantInTournamentException("Cannot delete participant: "+participant.toString()+", is in at least one tournament.");
        }
        teamRepository.delete(id);
        participantRepository.delete(participant);
    }
    
}
