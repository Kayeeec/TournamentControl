/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.ParticipantRepository;
import cz.tournament.control.repository.TeamRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
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

    public TeamService(UserRepository userRepository, TeamRepository teamRepository, ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.participantRepository = participantRepository;
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
    
}
