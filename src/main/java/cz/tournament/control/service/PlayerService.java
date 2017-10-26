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
import cz.tournament.control.repository.ParticipantRepository;
import cz.tournament.control.repository.PlayerRepository;
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
public class PlayerService {
    
    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final ParticipantRepository participantRepository;
    private final TournamentService tournamentService;
    private final TeamService teamService;

    public PlayerService(UserRepository userRepository, PlayerRepository playerRepository, ParticipantRepository participantRepository, TournamentService tournamentService, TeamService teamService) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.participantRepository = participantRepository;
        this.tournamentService = tournamentService;
        this.teamService = teamService;
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
    public List<Player> findAll(){
        List<Player> players = playerRepository.findByUserIsCurrentUser();
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
     * @param id of entity to delete
     * @throws cz.tournament.control.domain.exceptions.ParticipantInTournamentException
     */
    public void delete(Long id) throws ParticipantInTournamentException{
        Participant participant = participantRepository.findByPlayer(playerRepository.findOne(id));
        List<Tournament> hisTournaments = tournamentService.findByParticipant(participant);
        if(!hisTournaments.isEmpty()){
            throw new ParticipantInTournamentException("Cannot delete participant: "+participant.toString()+", is in at least one tournament.");
        }
        //break relationship with team
        Player player = playerRepository.getOne(id);
        Team[] teamsArr = player.getTeams().toArray(new Team[player.getTeams().size()]);
        for (int i = 0; i < teamsArr.length; i++) {
            player.removeTeams(teamsArr[i]);
            teamService.updateTeam(teamsArr[i]);
        }
        playerRepository.save(player);
        playerRepository.delete(id);
        participantRepository.delete(participant);
    }
    
    
    
}
