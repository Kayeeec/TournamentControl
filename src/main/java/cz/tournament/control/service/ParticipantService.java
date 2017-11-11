/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.ParticipantRepository;
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
public class ParticipantService {
    private final Logger log = LoggerFactory.getLogger(ParticipantService.class);
    
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    public ParticipantService(UserRepository userRepository, ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
    }
    
    public Participant createParticipant(Participant participant){
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        participant.user(creator);
        Participant result = participantRepository.save(participant);
        return result;
    }
    
    public Participant updateParticipant(Participant participant){
        Participant result = participantRepository.save(participant);
        return result;
    }
    
    public Participant getByeParticipant(){
        List<Participant> fromDB = participantRepository.findByTeamIsNullAndPlayerIsNullAndUserIsCurrentUser();
        if(fromDB.size() > 0){
            return fromDB.get(0);
        }
        Participant result = createParticipant(new Participant());
        return result;
    }
    
    @Transactional(readOnly = true)
    public Participant findByTeam(Team team){
        return participantRepository.findByTeam(team);
        
    }
    
    @Transactional(readOnly = true)
    public Participant findByPlayer(Player player){
        return participantRepository.findByPlayer(player);
        
    }
    
    
    
}
