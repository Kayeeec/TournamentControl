/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
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
public class TournamentService {
    private final Logger log = LoggerFactory.getLogger(TournamentService.class);

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;

    public TournamentService(UserRepository userRepository, TournamentRepository tournamentRepository) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
    }
    
    public Tournament createTournament(Tournament tournament){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        tournament.setUser(creator);
        //set creation date
        tournament.setCreated(ZonedDateTime.now());
        
        Tournament result = tournamentRepository.save(tournament);
        log.debug("TOUTNAMENT_SERVICE: Created Tournament: {}", result);
        return result;   
    }
    
    /**
     *  Get one game by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Tournament findOne(Long id) {
        log.debug("Request to get Tournament : {}", id);
        Tournament tournament = tournamentRepository.findOne(id);
        return tournament;
    }
    
    
    
    
}
