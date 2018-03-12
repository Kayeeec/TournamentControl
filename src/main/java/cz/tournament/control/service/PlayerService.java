/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.domain.exceptions.ParticipantInTournamentException;
import cz.tournament.control.repository.CombinedRepository;
import cz.tournament.control.repository.ParticipantRepository;
import cz.tournament.control.repository.PlayerRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Karolina Bozkova
 */
@Service
@Transactional
public class PlayerService {
    
    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final ParticipantRepository participantRepository;
    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final CombinedRepository combinedRepository;

    public PlayerService(UserRepository userRepository, PlayerRepository playerRepository, ParticipantRepository participantRepository, TournamentService tournamentService, TeamService teamService, CombinedRepository combinedRepository) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.participantRepository = participantRepository;
        this.tournamentService = tournamentService;
        this.teamService = teamService;
        this.combinedRepository = combinedRepository;
    }
    
    public Player save(Player player){
        return playerRepository.save(player);
    }

    
    public Player createPlayer(Player player){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        player.setUser(creator);
        
        Player result = playerRepository.save(player);
        
        Participant participant = participantRepository.save(new Participant(result, creator));
        
        log.debug("PLAYER_SERVICE: Created Player: {}", result);
        log.debug("PLAYER_SERVICE: Created Participant: {}", participant);
        
        return result;
        
    }
    
    public Player updatePlayer(Player player){
        Player result = playerRepository.save(player);
        log.debug("PLAYER_SERVICE: Updated Player: {}", result);
        return result;
    }
    
    @Transactional(readOnly = true)
    public Page<Player> findAll(Pageable pageable){
        Page<Player> players = playerRepository.findByUserIsCurrentUser(pageable);
        return players;
    }
    
    @Transactional(readOnly = true)
    public Player findOne(Long id){
        Player player = playerRepository.findOne(id);
        return player;
    }
    
    @Transactional(readOnly = true)
    public List<Player> findByUser(User user){
        List<Player> players = playerRepository.findByUser(user);
        return players;
    }
    
    @Transactional(readOnly = true)
    public List<Tournament> findAllTournaments(Player player){
        Participant parfticipant = participantRepository.findByPlayer(player);
        return tournamentService.findByParticipant(parfticipant);
    }
    
    /**
     * Deletes a player entity and its associated participant entity.
     * 
     * No participant can be deleted when it still is in any 
     * tournament -> throws exception that is caught and user is properly informed on front end
     * 
     * Player is simply removed from team.
     * Player teams have to be fetched form db separately because db and 
     * player instance might not be in sync.
     * 
     * @param id of entity to delete
     * @throws cz.tournament.control.domain.exceptions.ParticipantInTournamentException
     */
    public void delete(Long id) throws ParticipantInTournamentException{
        log.debug("deleting player with id: {}", id);
        Player player = findOne(id);
        Participant participant = participantRepository.findByPlayer(player);
        
        //check if its in any tournaments 
        if(!tournamentService.findByParticipant(participant).isEmpty() 
                || !combinedRepository.findByAllParticipantsContains(participant).isEmpty()){
            throw new ParticipantInTournamentException(participant);
        }
        //remove itself from each team 
        List<Team> teams = teamService.findTeamsForPlayer(player);
        for (Team team : teams) {
            teamService.updateTeam(team.removeMembers(player));
        }
        
        playerRepository.save(player);
        playerRepository.delete(id);
        participantRepository.delete(participant);
    }
    
    
    
}
