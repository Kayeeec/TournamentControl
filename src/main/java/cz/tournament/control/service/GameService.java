/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.repository.GameRepository;
import java.util.ArrayList;
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
public class GameService {
    private final Logger log = LoggerFactory.getLogger(GameService.class);
    private final GameRepository gameRepository;
    private final GameSetService gameSetService;

    public GameService(GameRepository gameRepository, GameSetService gameSetService) {
        this.gameRepository = gameRepository;
        this.gameSetService = gameSetService;
    }

    
    //TODO : implement creating default sets and adding sets?
    public Game createGame(Game game){
        log.debug("Request to create Game : {}", game);
        
        Game tmp = gameRepository.save(game);
        
        //prepare sets - one or basic number of sets 
        GameSet set = gameSetService.save(new GameSet().game(tmp));
        tmp.addSets(set);
        Integer sets = tmp.getTournament().getNumberOfSets();
        if(sets != null && sets > 1){
            for (int i = 1; i < sets; i++) {
                set = gameSetService.save(new GameSet().game(tmp));
                tmp.addSets(set);
            }
        }
        
        Game result = gameRepository.save(tmp);
        return result;
    }
    
    public Game updateGame(Game game){
        log.debug("Request to update Game : {}", game);
        
        //ensure tournament
        Tournament oldTournament = gameRepository.findOne(game.getId()).getTournament();
        game.setTournament(oldTournament);
        log.debug("old tournament: {}", oldTournament);
        
        Game result = gameRepository.save(game);
        return result;
    }
    
    public Game addNewSet(Game game){
        GameSet set = gameSetService.save(new GameSet().game(game));
        log.debug("GAME_SERVICE..........added set: {}, game= {}",set.getId(), game.toString());
        game.addSets(set);
        Game result = gameRepository.save(game);
        log.debug("GAME_SERVICE: saved game: {} with sets {}", game.toString(), game.setsToString());
        return result;
    }
    
    /**
     *  Get one game by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Game findOne(Long id) {
        log.debug("Request to get Game : {}", id);
        Game game = gameRepository.findOne(id);
        return game;
    }
    
    /**
     *  Get all the games.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        log.debug("Request to get all Games");
        List<Game> result = gameRepository.findAll();
        return result;
    }
    
    /**
     *  Get all the games.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Game> findGamesByTournament(Tournament tournament) {
        log.debug("Request to get all Games with tournament: {}", tournament);
        List<Game> result = gameRepository.findByTournament(tournament);
        return result;
    }
    
     /**
     *  Delete the game by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Game : {}", id);
        Game game = gameRepository.findOne(id);
        gameSetService.delete(new ArrayList<>(game.getSets()));
        gameRepository.delete(id);
    }
    
    /**
     *  Delete list of games.
     *
     * 
     * @param games list of entities to delete 
     */
    public void delete(List<Game> games) {
        log.debug("Request to delete list of games : {}", games.toString());
        for (Game game : games) {
            gameSetService.delete(new ArrayList<>(game.getSets()));
        }
        gameRepository.delete(games);
    }
    
}
