package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.repository.GameRepository;
import cz.tournament.control.repository.GameSetRepository;
import cz.tournament.control.repository.SetSettingsRepository;
import cz.tournament.control.repository.TournamentRepository;
import java.util.Collections;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Service Implementation for managing GameSet.
 */
@Service
@Transactional
public class GameSetService {

    private final Logger log = LoggerFactory.getLogger(GameSetService.class);
    
    private final GameSetRepository gameSetRepository;
    private final GameRepository gameRepository;
    private final TournamentRepository tournamentRepository;
    private final SetSettingsRepository setSettingsRepository;

    public GameSetService(GameSetRepository gameSetRepository, GameRepository gameRepository, TournamentRepository tournamentRepository, SetSettingsRepository setSettingsRepository) {
        this.gameSetRepository = gameSetRepository;
        this.gameRepository = gameRepository;
        this.tournamentRepository = tournamentRepository;
        this.setSettingsRepository = setSettingsRepository;
    }
    
    /**
     * Save a gameSet.
     *
     * @param gameSet the entity to save
     * @return the persisted entity
     */
    public GameSet save(GameSet gameSet) {
        log.debug("Request to save GameSet : {}", gameSet);
        GameSet result = gameSetRepository.save(gameSet);
        return result;
    }
    
    public GameSet updateGameSet(GameSet gameSet){
        log.debug("Request to update GameSet : {}", gameSet);
        
//        if(gameSet.getSetSettings().getId() == null){
//            gameSet.setSettings(setSettingsRepository.save(gameSet.getSetSettings()));
//        }
        if(!Objects.equals(gameSet.getSetSettings().getId(), gameSet.getGame().getTournament().getSetSettings().getId())){
            gameSet.setSettings(setSettingsRepository.save(gameSet.getSetSettings()));
        }
        
        GameSet result = gameSetRepository.save(gameSet);
        return result;
    }
    
    public Game changeSetSettings(GameSet gameSet){
        GameSet set = gameSetRepository.findOne(gameSet.getId());
        
        SetSettings defaultSettings = set.getGame().getTournament().getSetSettings();
        SetSettings newSettings = gameSet.getSetSettings();
        if(Objects.equals(defaultSettings.getId(), newSettings.getId())){
            newSettings.setId(null);
        }
        
        SetSettings savedSettings = setSettingsRepository.save(newSettings);
        set.setSetSettings(savedSettings);
        GameSet saved = gameSetRepository.save(set);
        Game game = saved.getGame();
        return game; 
    }

    /**
     *  Get all the gameSets.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<GameSet> findAll() {
        log.debug("Request to get all GameSets");
        List<GameSet> result = gameSetRepository.findAll();

        return result;
    }
    
    /**
     *  Get all gameSets for given game.
     *  
     * @param game
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<GameSet> findGameSetsByGame(Game game) {
        log.debug("Request to get all GameSets for game: {}", game);
        List<GameSet> result = gameSetRepository.findByGame(game);
        Collections.sort(result, Comparator.comparingLong(h -> h.getId()));
        return result;
    }

    /**
     *  Get one gameSet by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public GameSet findOne(Long id) {
        log.debug("Request to get GameSet : {}", id);
        GameSet gameSet = gameSetRepository.findOne(id);
        return gameSet;
    }

    /**
     *  Delete the  gameSet by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete GameSet : {}", id);
        GameSet set = gameSetRepository.findOne(id);
        
        //delete its setSettings if they are not the default one (tournament.setSettings) 
        //  sets have either the default settings or their own
        SetSettings setSettings = set.getSetSettings();
        if(!set.getGame().getTournament().getSetSettings().equals(setSettings)){
            setSettingsRepository.delete(setSettings.getId());
        }
        
//        //remove this set from its game
//        Game game = set.getGame();
//        game.removeSets(set);
//        Game savedGame = gameRepository.save(game);
        
        //delete set
        gameSetRepository.delete(id);
    }
    
    public void delete(Iterable<GameSet> gameSets){
        log.debug("Request to delete GameSets : {}", gameSets.toString());
        gameSetRepository.delete(gameSets);
        
    }
}
