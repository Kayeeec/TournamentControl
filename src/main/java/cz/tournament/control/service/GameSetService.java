package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.repository.GameRepository;
import cz.tournament.control.repository.GameSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing GameSet.
 */
@Service
@Transactional
public class GameSetService {

    private final Logger log = LoggerFactory.getLogger(GameSetService.class);
    
    private final GameSetRepository gameSetRepository;
    private final GameRepository gameRepository;

    public GameSetService(GameSetRepository gameSetRepository, GameRepository gameRepository) {
        this.gameSetRepository = gameSetRepository;
        this.gameRepository = gameRepository;
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
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<GameSet> findGameSetsByGame(Game game) {
        log.debug("Request to get all GameSets for game: {}", game);
        List<GameSet> result = gameSetRepository.findByGame(game);

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
    public Game delete(Long id) {
        log.debug("Request to delete GameSet : {}", id);
        GameSet set = gameSetRepository.findOne(id);
        Game game = set.getGame();
        game.removeSets(set);
        Game savedGame = gameRepository.save(game);
        log.debug("Game after deleting set: {}", savedGame);
        gameSetRepository.delete(id);
        return savedGame;
    }
    
    public void delete(List<GameSet> gameSets){
        log.debug("Request to delete GameSets : {}", gameSets.toString());
        gameSetRepository.delete(gameSets);
    }
}
