/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.domain.SetSettings;
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
    private final SetSettingsService setSettingsService;

    public GameService(GameRepository gameRepository, GameSetService gameSetService, SetSettingsService setSettingsService) {
        this.gameRepository = gameRepository;
        this.gameSetService = gameSetService;
        this.setSettingsService = setSettingsService;
    }

    
    
    public Game createGame(Game game){
        log.debug("Request to create Game : {}", game);
        
        Game tmp = gameRepository.save(game);
        SetSettings defaultSetSettings = setSettingsService.findOne(game.getTournament().getSetSettings().getId()) ;
        
        //prepare sets - one or number of sets to win
        GameSet set = gameSetService.save(new GameSet().game(tmp).setSettings(defaultSetSettings));
        tmp.addSets(set);
        Integer sets = tmp.getTournament().getSetsToWin();
        if(sets != null && sets > 1){
            for (int i = 1; i < sets; i++) {
                set = gameSetService.save(new GameSet().game(tmp).setSettings(defaultSetSettings));
                tmp.addSets(set);
            }
        }
        
        Game result = gameRepository.save(tmp);
        return result;
    }
    
    public Game updateGame(Game game){
//        log.debug("Request to update Game : {}", game);
        
        //ensure tournament - never changes
        if(game.getTournament() == null){
           Tournament oldTournament = gameRepository.findOne(game.getId()).getTournament();
            game.setTournament(oldTournament); 
        }
        
        //finish the game if all sets are finished and is not an unallowed tie 
        if(game.getTournament().getTiesAllowed()){
            if(game.allSetsFinished()){
                game.setFinished(true);
            } 
        }else{
           if(game.getWinner() != null){
               game.setFinished(true);   
           } 
        }
        
        Game result = gameRepository.save(game);
        return result;
    }
    
    /**
     * Creates a new set for game with tournaments setSettings.
     * @param game
     * @return
     */
    public Game addNewSet(Game game){
        SetSettings setSettings = gameRepository.findOne(game.getId()).getTournament().getSetSettings();
        GameSet set = gameSetService.save(new GameSet().game(game).setSettings(setSettings));
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
     *  Get all the games that have a certain tournament.
     *  
     * @param tournament of games returned
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
