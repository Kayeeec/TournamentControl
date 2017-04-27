package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.service.SetSettingsService;
import cz.tournament.control.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing SetSettings.
 */
@RestController
@RequestMapping("/api")
public class SetSettingsResource {

    private final Logger log = LoggerFactory.getLogger(SetSettingsResource.class);

    private static final String ENTITY_NAME = "setSettings";
        
    private final SetSettingsService setSettingsService;

    public SetSettingsResource(SetSettingsService setSettingsService) {
        this.setSettingsService = setSettingsService;
    }

    /**
     * POST  /set-settings : Create a new setSettings.
     *
     * @param setSettings the setSettings to create
     * @return the ResponseEntity with status 201 (Created) and with body the new setSettings, or with status 400 (Bad Request) if the setSettings has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/set-settings")
    @Timed
    public ResponseEntity<SetSettings> createSetSettings(@Valid @RequestBody SetSettings setSettings) throws URISyntaxException {
        log.debug("REST request to save SetSettings : {}", setSettings);
        if (setSettings.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new setSettings cannot already have an ID")).body(null);
        }
        SetSettings result = setSettingsService.save(setSettings);
        return ResponseEntity.created(new URI("/api/set-settings/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /set-settings : Updates an existing setSettings.
     *
     * @param setSettings the setSettings to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated setSettings,
     * or with status 400 (Bad Request) if the setSettings is not valid,
     * or with status 500 (Internal Server Error) if the setSettings couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/set-settings")
    @Timed
    public ResponseEntity<SetSettings> updateSetSettings(@Valid @RequestBody SetSettings setSettings) throws URISyntaxException {
        log.debug("REST request to update SetSettings : {}", setSettings);
        if (setSettings.getId() == null) {
            return createSetSettings(setSettings);
        }
        SetSettings result = setSettingsService.save(setSettings);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, setSettings.getId().toString()))
            .body(result);
    }

    /**
     * GET  /set-settings : get all the setSettings.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of setSettings in body
     */
    @GetMapping("/set-settings")
    @Timed
    public List<SetSettings> getAllSetSettings() {
        log.debug("REST request to get all SetSettings");
        return setSettingsService.findAll();
    }

    /**
     * GET  /set-settings/:id : get the "id" setSettings.
     *
     * @param id the id of the setSettings to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the setSettings, or with status 404 (Not Found)
     */
    @GetMapping("/set-settings/{id}")
    @Timed
    public ResponseEntity<SetSettings> getSetSettings(@PathVariable Long id) {
        log.debug("REST request to get SetSettings : {}", id);
        SetSettings setSettings = setSettingsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(setSettings));
    }

    /**
     * DELETE  /set-settings/:id : delete the "id" setSettings.
     *
     * @param id the id of the setSettings to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/set-settings/{id}")
    @Timed
    public ResponseEntity<Void> deleteSetSettings(@PathVariable Long id) {
        log.debug("REST request to delete SetSettings : {}", id);
        setSettingsService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
