package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.service.SwissService;
import cz.tournament.control.service.dto.SwissDTO;
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

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Swiss.
 */
@RestController
@RequestMapping("/api")
public class SwissResource {

    private final Logger log = LoggerFactory.getLogger(SwissResource.class);

    private static final String ENTITY_NAME = "swiss";

    private final SwissService swissService;

    public SwissResource(SwissService swissService) {
        this.swissService = swissService;
    }

    /**
     * POST  /swisses : Create a new swiss.
     *
     * @param swissDTO the swissDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new swiss, or with status 400 (Bad Request) if the swiss has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/swisses")
    @Timed
    public ResponseEntity<Swiss> createSwiss(@RequestBody SwissDTO swissDTO) throws URISyntaxException {
        log.debug("REST request to save SwissDTO : {}", swissDTO.toString());
        if (swissDTO.getSwiss().getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new swiss cannot already have an ID")).body(null);
        }
        Swiss result = swissService.createSwiss(swissDTO);
        return ResponseEntity.created(new URI("/api/swisses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /swisses : Updates an existing swiss.
     *
     * @param swissDTO the swissDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated swiss,
     * or with status 400 (Bad Request) if the swiss is not valid,
     * or with status 500 (Internal Server Error) if the swiss couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/swisses")
    @Timed
    public ResponseEntity<Swiss> updateSwiss(@RequestBody SwissDTO swissDTO) throws URISyntaxException {
        log.debug("REST request to update SwissDTO : {}", swissDTO.toString());
        if (swissDTO.getSwiss().getId() == null) {
            return createSwiss(swissDTO);
        }
        Swiss result = swissService.updateSwiss(swissDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, swissDTO.getSwiss().getId().toString()))
            .body(result);
    }
    
    /**
     * POST /swisses/generate : generates next round of an existing swiss
     * @param swiss - to generate next round to
     * @return the ResponseEntity with status 200 (OK) and with body the updated swiss,
     * or with status 400 (Bad Request) if the swiss is not valid,
     * or with status 500 (Internal Server Error) if the swiss couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/swisses/generate")
    @Timed
    public ResponseEntity<Swiss> generateNextRound(@RequestBody Swiss swiss) throws URISyntaxException{
        log.debug("REST request to generate next round Swiss : {}", swiss.toString());
        Swiss result = swissService.generateNextRound(swiss);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, swiss.getId().toString()))
            .body(result);  
    }
    
    /**
     * GET  /swisses/seeding/{id} : get seeding of an existing swiss
     * @param id of an existing swiss to get seeding of
     * @return List od Participant objects
     * or empty list if no seeding found.
     */
    @GetMapping("/swisses/seeding/{id}")
    @Timed
    public List<Participant> getSwissSeeding(@PathVariable Long id) {
        log.debug("REST request to get seeding of Swiss: {}", id);
        List<Participant> seeding = swissService.getSwissSeeding(id);
        return seeding;
    }

    /**
     * GET  /swisses : get all the swisses.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of swisses in body
     */
    @GetMapping("/swisses")
    @Timed
    public ResponseEntity<List<Swiss>> getAllSwisses(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Swisses");
        Page<Swiss> page = swissService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/swisses");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /swisses/:id : get the "id" swiss.
     *
     * @param id the id of the swiss to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the swiss, or with status 404 (Not Found)
     */
    @GetMapping("/swisses/{id}")
    @Timed
    public ResponseEntity<Swiss> getSwiss(@PathVariable Long id) {
        log.debug("REST request to get Swiss : {}", id);
        Swiss swiss = swissService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(swiss));
    }

    /**
     * DELETE  /swisses/:id : delete the "id" swiss.
     *
     * @param id the id of the swiss to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/swisses/{id}")
    @Timed
    public ResponseEntity<Void> deleteSwiss(@PathVariable Long id) {
        log.debug("REST request to delete Swiss : {}", id);
        swissService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
