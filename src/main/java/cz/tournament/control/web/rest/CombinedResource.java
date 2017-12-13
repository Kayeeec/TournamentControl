package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.service.CombinedService;
import cz.tournament.control.service.dto.CombinedDTO;
import cz.tournament.control.web.rest.errors.BadRequestAlertException;
import cz.tournament.control.web.rest.util.HeaderUtil;
import cz.tournament.control.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Combined.
 */
@RestController
@RequestMapping("/api")
public class CombinedResource {

    private final Logger log = LoggerFactory.getLogger(CombinedResource.class);

    private static final String ENTITY_NAME = "combined";

    private final CombinedService combinedService;

    public CombinedResource(CombinedService combinedService) {
        this.combinedService = combinedService;
    }

    /**
     * POST  /combined : Create a new combined.
     *
     * @param combinedDTO the combined to create
     * @return the ResponseEntity with status 201 (Created) and with body the new combined, or with status 400 (Bad Request) if the combined has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/combined")
    @Timed
    public ResponseEntity<Combined> createCombined(@Valid @RequestBody CombinedDTO combinedDTO) throws URISyntaxException {
        log.debug("REST request to save Combined : {}", combinedDTO);
        if (combinedDTO.getCombined().getId() != null) {
            throw new BadRequestAlertException("A new combined cannot already have an ID", ENTITY_NAME, "idexists");
        }
       Combined result = combinedService.createCombined(combinedDTO);
       
        return ResponseEntity.created(new URI("/api/combined/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /combined : Updates an existing combined.
     *
     * @param combinedDTO the combined to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated combined,
     * or with status 400 (Bad Request) if the combined is not valid,
     * or with status 500 (Internal Server Error) if the combined couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/combined")
    @Timed
    public ResponseEntity<Combined> updateCombined(@Valid @RequestBody CombinedDTO combinedDTO) throws URISyntaxException {
        log.debug("REST request to update Combined : {}", combinedDTO);
        if (combinedDTO.getCombined().getId() == null) {
            return createCombined(combinedDTO);
        }
        Combined result = combinedService.updateCombined(combinedDTO);
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    @PutMapping("/combined/generate-playoff")
    @Timed
    public ResponseEntity<Combined> generatePlayoff(@Valid @RequestBody Long id) throws URISyntaxException {
        log.debug("REST request to generate playoff for combined with id : {}", id);
        
        Combined result = combinedService.generatePlayoff(id);
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * GET  /combined : get all the combined.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of combined in body
     */
    @GetMapping("/combined")
    @Timed
    public ResponseEntity<List<Combined>> getAllCombineds(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Combineds");
        Page<Combined> page = combinedService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/combined");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /combined/:id : get the "id" combined.
     *
     * @param id the id of the combined to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the combined, or with status 404 (Not Found)
     */
    @GetMapping("/combined/{id}")
    @Timed
    public ResponseEntity<Combined> getCombined(@PathVariable Long id) {
        log.debug("REST request to get Combined : {}", id);
        Combined combined = combinedService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(combined));
    }
    
    @PostMapping("/combined/tournament")
    @Timed
    public ResponseEntity<Combined> getCombinedForTournament(@Valid @RequestBody Tournament tournament) {
        log.debug("REST request to get Combined for Tournament with id {}", tournament.getId());
        Combined combined = combinedService.findByTournament(tournament);
        
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(combined));
    }
    
    /**
     * DELETE  /combined/:id : delete the "id" combined.
     *
     * @param id the id of the combined to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/combined/{id}")
    @Timed
    public ResponseEntity<Void> deleteCombined(@PathVariable Long id) {
        log.debug("REST request to delete Combined : {}", id);
        combinedService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    
    
}
