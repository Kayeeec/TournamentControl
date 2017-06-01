/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.enumeration.EliminationType;
import cz.tournament.control.repository.GameRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final TournamentService tournamentService;

    public GameService(GameRepository gameRepository, GameSetService gameSetService, SetSettingsService setSettingsService, TournamentService tournamentService) {
        this.gameRepository = gameRepository;
        this.gameSetService = gameSetService;
        this.setSettingsService = setSettingsService;
        this.tournamentService = tournamentService;
    }
    
    public Game createGame(Game game){
        log.debug("Request to create Game : {}", game);
        
        Game tmp = gameRepository.save(game);
//        SetSettings defaultSetSettings = setSettingsService.findOne(game.getTournament().getSetSettings().getId()) ;
        SetSettings defaultSetSettings = setSettingsService.save(game.getTournament().getSetSettings()) ;

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
        if(game.getRivalA() != null && game.getRivalB() != null){
            if(game.getRivalA().isBye() || game.getRivalB().isBye()){
                game.setFinished(true);
            }
            if(game.getTournament().getTiesAllowed()){
                if(game.allSetsFinished()){
                    game.setFinished(true);
                } 
            }else{
               if(game.allSetsFinished_And_NotATie()){
                   game.setFinished(true);   
               } 
            }
            
        }
        
        //for Elimination update next games 
        eliminationNextGameUpdate(game);
        
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
    
    /*****************************/
    /*     update game logic     */
    /*****************************/
    
    private int getRootIndex(int N, Elimination tournament) {
        if (tournament.getType().equals(EliminationType.SINGLE)) {
            return (N - 2);
        } else { //(tournament.getType().equals(EliminationType.DOUBLE))
            return ((2 * N) - 3);
        }
    }
    
    private int getParentIndex(int index, int round, int N, Elimination tournament){
        int rootIndex = getRootIndex(N, tournament);
        
        if(tournament.getType().equals(EliminationType.SINGLE)){
            return (int) Math.floor((index + rootIndex)/2) + 1;
        }
        else{//DOUBLE
            int winnerRoot = (N - 2);
            int loserRoot = ((2*N)-4);
            
            if(index == winnerRoot || index == loserRoot ){
                return rootIndex;
            }
            if(index < winnerRoot){
                return (int) Math.floor((index + winnerRoot)/2)+1;
            }
            if(index < loserRoot){
                int segmentSize = N/((int)Math.pow(2, Math.ceil(((double)round)/10)));
                if(round % 10 != 0){ //15, 25, 35...
                    return (index + segmentSize);   
                }
                //else 20,30,40...
                int inSegmentIndex;
                if(index % 2 == 1){ 
                    inSegmentIndex = ((index + 1) % segmentSize)/2;
                    return index + segmentSize - inSegmentIndex;
                }else{
                    inSegmentIndex = ((index) % segmentSize)/2;
                    return (index-1) + segmentSize - inSegmentIndex;
                }
            }
            if(index == rootIndex){
                if(tournament.getBronzeMatch()){
                    return ((2*N)-1);
                }
                return ((2*N)-2);
            }
            return -1;
        }
    }
    
    private Game progresRival(Game game, Participant rival){
        if(game.getRivalA() == null){
            game.rivalA(rival);
            return this.updateGame(game);
        }
        if(game.getRivalB() == null){
            game.rivalB(rival);
            return this.updateGame(game);
        }
        return null;
    }
    
    private int getLoserRound(int round){
        if(round == 1){
            return 15;
        }
        return round*10;
    }
    private List<Game> getGamesForRound(int loserRound, List<Game> matches, int N){
        List<Game> games = new ArrayList<>();
        for (int i = N-1; i <= 2*N-4; i++) {
            Game game = matches.get(i);
            if(game.getRound()>loserRound){
                break;
            }
            if(game.getRound()==loserRound){
                games.add(game);
            }       
        }
        return games;
    }
    
    private Game progresLoserIntoFirstAvailableLoserGame(List<Game> gamesForRound, Participant loser){
        for (Game game : gamesForRound) {
            if(game.getRivalA() == null){
                game.rivalA(loser);
                return this.updateGame(game);
            }
            if(game.getRivalB() == null){
                game.rivalB(loser);
                return this.updateGame(game);
            }
        }
        return null;
    }
    

    private void eliminationNextGameUpdate(Game game) {
        log.debug("eliminationNextGameUpdate: called");
        Tournament tournament = tournamentService.findOne(game.getTournament().getId());
        if(tournament instanceof Elimination && game.isFinished()){
            Elimination elimination = (Elimination) tournament;
            log.debug("eliminationNextGameUpdate proceeded");
            
            List<Game> matches = new ArrayList<>(elimination.getMatches());
            Collections.sort(matches);
            
            int N = elimination.getN();
            int index = matches.indexOf(game);
            Map<String, Participant> winnerAndLoser = game.getWinnerAndLoser();
            
            //put winner into the next game and save it 
            int winnerGameIndex = getParentIndex(index, game.getRound(), N, elimination);
            log.debug("eliminationNextGameUpdate: winnerGameIndex = {}", winnerGameIndex);
            if(winnerGameIndex != -1){
                Game savedWinnerGame = progresRival(matches.get(winnerGameIndex), winnerAndLoser.get("winner"));
                log.debug("eliminationNextGameUpdate: winner game with id = {} updated ", savedWinnerGame.getId());
            }
            
            //if Double - put loser into apropriate loser game 
            if(elimination.getType().equals(EliminationType.DOUBLE)){
                int loserRound = getLoserRound(game.getRound());
                Game savedLooserGame = progresLoserIntoFirstAvailableLoserGame(
                        getGamesForRound(loserRound, matches, N), 
                        winnerAndLoser.get("loser")
                ); 
            }
        }     
    }
}
