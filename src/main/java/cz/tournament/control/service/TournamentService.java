/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final GameService gameService;

    public TournamentService(UserRepository userRepository, TournamentRepository tournamentRepository, GameService gameService) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
        this.gameService = gameService;
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
    
    public Tournament updateTournament(Tournament tournament){
        Tournament result = tournamentRepository.save(tournament);
        return result;
    }
    
    public List<Tournament> findAll(){
        List<Tournament> tournaments = tournamentRepository.findByUserIsCurrentUser();
        return tournaments;
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
        Tournament tournament = tournamentRepository.findOneWithEagerRelationships(id);
        return tournament;
    }
    
    public void delete(Long id){
        Tournament tournament = tournamentRepository.findOneWithEagerRelationships(id);
        List<Game> games = new ArrayList<>(tournament.getMatches());
        gameService.delete(games);
        tournamentRepository.delete(id);
    }
    
    
    
    
}
