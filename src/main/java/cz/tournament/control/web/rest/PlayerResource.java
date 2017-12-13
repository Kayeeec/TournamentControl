package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.exceptions.ParticipantInTournamentException;
import cz.tournament.control.service.CombinedService;
import cz.tournament.control.service.PlayerService;
import cz.tournament.control.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;

/**
 * REST controller for managing Player.
 */
@RestController
@RequestMapping("/api")
public class PlayerResource {

    private final Logger log = LoggerFactory.getLogger(PlayerResource.class);

    private static final String ENTITY_NAME = "player";
    
    private final PlayerService playerService;
    private final CombinedService combinedService;

    public PlayerResource(PlayerService playerService, CombinedService combinedService) {
        this.playerService = playerService;
        this.combinedService = combinedService;
    }


    /**
     * POST  /players : Create a new player.
     *
     * @param player the player to create
     * @return the ResponseEntity with status 201 (Created) and with body the new player, or with status 400 (Bad Request) if the player has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/players")
    @Timed
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody Player player) throws URISyntaxException {
        log.debug("REST request to save Player : {}", player);
        if (player.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new player cannot already have an ID")).body(null);
        }
        
        Player result = playerService.createPlayer(player);
        
        return ResponseEntity.created(new URI("/api/players/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /players : Updates an existing player.
     *
     * @param player the player to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated player,
     * or with status 400 (Bad Request) if the player is not valid,
     * or with status 500 (Internal Server Error) if the player couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/players")
    @Timed
    public ResponseEntity<Player> updatePlayer(@Valid @RequestBody Player player) throws URISyntaxException {
        log.debug("REST request to update Player : {}", player);
        if (player.getId() == null) {
            return createPlayer(player);
        }
        Player result = playerService.updatePlayer(player);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, player.getId().toString()))
            .body(result);
    }

    /**
     * GET  /players : get all the players.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of players in body
     */
    @GetMapping("/players")
    @Timed
    public List<Player> getAllPlayers() {
        log.debug("REST request to get all Players");
        List<Player> players = playerService.findAll();
        return players;
    }
    
    /**
     * Gets all tournaments where player is a participant.
     * @param id of a player to find tournaments to
     * @return list of tournaments
     */
    @GetMapping("/players/tournaments/{id}")
    @Timed
    public List<Tournament> getAllTournaments(@PathVariable Long id){
        if(id == null){return new ArrayList<>();}
        
        Player player = playerService.findOne(id);
        return playerService.findAllTournaments(player);
    }
    
    @GetMapping("/players/combined/{id}")
    @Timed
    public List<Combined> getAllCombinedTournaments(@PathVariable Long id){
        if(id == null){return new ArrayList<>();}
        
        Player player = playerService.findOne(id);
        return combinedService.findByPlayer(player);
    }
    
    /**
     * Gets teams for player, because teams attribute is ignored in json.
     * @param id of a player to find teams to
     * @return list of teams 
     */
    @GetMapping("/players/teams/{id}")
    @Timed
    public List<Team> getTeams(@PathVariable Long id){
        if(id == null){return new ArrayList<>();}
        
        Player player = playerService.findOne(id);
        return new ArrayList<>(player.getTeams());
    }

    /**
     * GET  /players/:id : get the "id" player.
     *
     * @param id the id of the player to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the player, or with status 404 (Not Found)
     */
    @GetMapping("/players/{id}")
    @Timed
    public ResponseEntity<Player> getPlayer(@PathVariable Long id) {
        log.debug("REST request to get Player : {}", id);
        Player player = playerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(player));
    }

    /**
     * DELETE  /players/:id : delete the "id" player.
     *
     * @param id the id of the player to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/players/{id}")
    @Timed
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        log.debug("REST request to delete Player : {}", id);
        
        try {
            playerService.delete(id);
        } catch (ParticipantInTournamentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).
                    headers(HeaderUtil.
                            createFailureAlert(ENTITY_NAME, "participantInTournament", ex.getMessage())).build();
        }
        
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
