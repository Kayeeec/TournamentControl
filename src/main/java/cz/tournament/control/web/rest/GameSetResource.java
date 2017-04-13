package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.service.GameService;
import cz.tournament.control.service.GameSetService;
import cz.tournament.control.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing GameSet.
 */
@RestController
@RequestMapping("/api")
public class GameSetResource {

    private final Logger log = LoggerFactory.getLogger(GameSetResource.class);

    private static final String ENTITY_NAME = "gameSet";
        
    private final GameSetService gameSetService;
    private final GameService gameService;

    public GameSetResource(GameSetService gameSetService, GameService gameService) {
        this.gameSetService = gameSetService;
        this.gameService = gameService;
    }

    

    /**
     * POST  /game-sets : Create a new gameSet.
     *
     * @param gameSet the gameSet to create
     * @return the ResponseEntity with status 201 (Created) and with body the new gameSet, or with status 400 (Bad Request) if the gameSet has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/game-sets")
    @Timed
    public ResponseEntity<GameSet> createGameSet(@RequestBody GameSet gameSet) throws URISyntaxException {
        log.debug("REST request to save GameSet : {}", gameSet);
        if (gameSet.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new gameSet cannot already have an ID")).body(null);
        }
        GameSet result = gameSetService.save(gameSet);
        return ResponseEntity.created(new URI("/api/game-sets/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /game-sets : Updates an existing gameSet.
     *
     * @param gameSet the gameSet to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated gameSet,
     * or with status 400 (Bad Request) if the gameSet is not valid,
     * or with status 500 (Internal Server Error) if the gameSet couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/game-sets")
    @Timed
    public ResponseEntity<GameSet> updateGameSet(@RequestBody GameSet gameSet) throws URISyntaxException {
        log.debug("REST request to update GameSet : {}", gameSet);
        if (gameSet.getId() == null) {
            return createGameSet(gameSet);
        }
        GameSet result = gameSetService.save(gameSet);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, gameSet.getId().toString()))
            .body(result);
    }

    /**
     * GET  /game-sets : get all the gameSets.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of gameSets in body
     */
    @GetMapping("/game-sets")
    @Timed
    public List<GameSet> getAllGameSets() {
        log.debug("REST request to get all GameSets");
        return gameSetService.findAll();
    }
    
    /**
     * GET  /game-sets : get all the gameSets for given game.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of gameSets in body
     */
    @GetMapping("/game-sets-for-game/{gameId}")
    @Timed
    public List<GameSet> getGameSetsForGame(@PathVariable Long gameId) {
        log.debug("REST request to get all GameSets");
        Game game = gameService.findOne(gameId);
        return gameSetService.findGameSetsByGame(game);
    }

    /**
     * GET  /game-sets/:id : get the "id" gameSet.
     *
     * @param id the id of the gameSet to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the gameSet, or with status 404 (Not Found)
     */
    @GetMapping("/game-sets/{id}")
    @Timed
    public ResponseEntity<GameSet> getGameSet(@PathVariable Long id) {
        log.debug("REST request to get GameSet : {}", id);
        GameSet gameSet = gameSetService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(gameSet));
    }

    /**
     * DELETE  /game-sets/:id : delete the "id" gameSet.
     *
     * @param id the id of the gameSet to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/game-sets/{id}")
    @Timed
    public ResponseEntity<Void> deleteGameSet(@PathVariable Long id) {
        log.debug("REST request to delete GameSet : {}", id);
        gameSetService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
