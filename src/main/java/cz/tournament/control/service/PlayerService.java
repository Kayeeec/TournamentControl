/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.User;
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

    public PlayerService(UserRepository userRepository, PlayerRepository playerRepository, ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.participantRepository = participantRepository;
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
    
    public List<Player> findAll(){
        List<Player> players = playerRepository.findByUserIsCurrentUser();
        return players;
    }
    
    public Player findOne(Long id){
        Player player = playerRepository.findOne(id);
        return player;
    }
    
    /**
     * Deletes a player entity and its associated participant entity.
     * 
     * @param id of entity to delete
     */
    public void delete(Long id){
        Participant participant = participantRepository.findByPlayer(playerRepository.findOne(id));
        playerRepository.delete(id);
        participantRepository.delete(participant);
    }
    
    
    
}
