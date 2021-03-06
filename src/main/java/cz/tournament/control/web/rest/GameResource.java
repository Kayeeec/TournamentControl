package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.service.GameService;
import cz.tournament.control.service.TournamentService;
import cz.tournament.control.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Game.
 */
@RestController
@RequestMapping("/api")
public class GameResource {

    private final Logger log = LoggerFactory.getLogger(GameResource.class);

    private static final String ENTITY_NAME = "game";
        
    private final GameService gameService;
    private final TournamentService tournamentService;

    public GameResource(GameService gameService, TournamentService tournamentService) {
        this.gameService = gameService;
        this.tournamentService = tournamentService;
    }

   
    
    /**
     * POST  /games : Create a new game.
     *
     * @param game the game to create
     * @return the ResponseEntity with status 201 (Created) and with body the new game, or with status 400 (Bad Request) if the game has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/games")
    @Timed
    public ResponseEntity<Game> createGame(@Valid @RequestBody Game game) throws URISyntaxException {
        log.debug("REST request to save Game : {}", game);
        if (game.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new game cannot already have an ID")).body(null);
        }
        Game result = gameService.createGame(game);
        
        return ResponseEntity.created(new URI("/api/games/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /games : Updates an existing game.
     *
     * @param game the game to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated game,
     * or with status 400 (Bad Request) if the game is not valid,
     * or with status 500 (Internal Server Error) if the game couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/games")
    @Timed
    public ResponseEntity<Game> updateGame(@Valid @RequestBody Game game) throws URISyntaxException {
        log.debug("REST request to update Game : {}", game);
        if (game.getId() == null) {
            return createGame(game);
        }
        
        Game result = gameService.updateGame(game);
        
        log.debug("REST request to update SAVED result : {}", result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, game.getId().toString()))
            .body(result);
    }
    
    /**
     * PUT  /games : Adds a new Set an existing game.
     *
     * @param game the game to add set to
     * @return the ResponseEntity with status 200 (OK) and with body the updated game,
     * or with status 400 (Bad Request) if the game is not valid,
     * or with status 500 (Internal Server Error) if the game couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/games/{gameId}/sets")
    @Timed
    public ResponseEntity<Game> addSet(@PathVariable Long gameId) throws URISyntaxException {
        log.debug("REST request to ADD SET to a Game with id: {}", gameId);
        Game game = gameService.findOne(gameId);
        Game result = gameService.addNewSet(game);
        
        log.debug("REST request to add set - SAVED result : {}", result);
//        return ResponseEntity.ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, game.getId().toString()))
//            .body(result);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(result));
    }
    
    /**
     * GET  /games : get all the games.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of games in body
     */
    @GetMapping("/games")
    @Timed
    public List<Game> getAllGames() {
        log.debug("REST request to get all Games");
        List<Game> games = gameService.findAll();
        return games;
    }
    
    /**
     * GET  /games : get all the games.
     *
     * @param tournamentId 
     * @return the ResponseEntity with status 200 (OK) and the list of games in body
     */
    @GetMapping("/games-by-tournament/{tournamentId}")
    @Timed
    public List<Game> getGamesByTournament(@PathVariable Long tournamentId) {
        log.debug("REST request to get all Games by tournament with id: {}", tournamentId);
        Tournament tournament = tournamentService.findOne(tournamentId);
        List<Game> games = gameService.findGamesByTournament(tournament);
        return games;
    }

    
    /**
     * GET  /games/:id : get the "id" game.
     *
     * @param id the id of the game to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the game, or with status 404 (Not Found)
     */
    @GetMapping("/games/{id}")
    @Timed
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        log.debug("REST request to get Game : {}", id);
        Game game = gameService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(game));
    }

    /**
     * DELETE  /games/:id : delete the "id" game.
     *
     * @param id the id of the game to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/games/{id}")
    @Timed
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        log.debug("REST request to delete Game : {}", id);
        gameService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
