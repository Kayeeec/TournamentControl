package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Combined;
import cz.tournament.control.service.CombinedService;
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
     * POST  /combineds : Create a new combined.
     *
     * @param combined the combined to create
     * @return the ResponseEntity with status 201 (Created) and with body the new combined, or with status 400 (Bad Request) if the combined has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/combineds")
    @Timed
    public ResponseEntity<Combined> createCombined(@Valid @RequestBody Combined combined) throws URISyntaxException {
        log.debug("REST request to save Combined : {}", combined);
        if (combined.getId() != null) {
            throw new BadRequestAlertException("A new combined cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Combined result = combinedService.save(combined);
        return ResponseEntity.created(new URI("/api/combineds/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /combineds : Updates an existing combined.
     *
     * @param combined the combined to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated combined,
     * or with status 400 (Bad Request) if the combined is not valid,
     * or with status 500 (Internal Server Error) if the combined couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/combineds")
    @Timed
    public ResponseEntity<Combined> updateCombined(@Valid @RequestBody Combined combined) throws URISyntaxException {
        log.debug("REST request to update Combined : {}", combined);
        if (combined.getId() == null) {
            return createCombined(combined);
        }
        Combined result = combinedService.save(combined);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, combined.getId().toString()))
            .body(result);
    }

    /**
     * GET  /combineds : get all the combineds.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of combineds in body
     */
    @GetMapping("/combineds")
    @Timed
    public ResponseEntity<List<Combined>> getAllCombineds(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Combineds");
        Page<Combined> page = combinedService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/combineds");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /combineds/:id : get the "id" combined.
     *
     * @param id the id of the combined to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the combined, or with status 404 (Not Found)
     */
    @GetMapping("/combineds/{id}")
    @Timed
    public ResponseEntity<Combined> getCombined(@PathVariable Long id) {
        log.debug("REST request to get Combined : {}", id);
        Combined combined = combinedService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(combined));
    }

    /**
     * DELETE  /combineds/:id : delete the "id" combined.
     *
     * @param id the id of the combined to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/combineds/{id}")
    @Timed
    public ResponseEntity<Void> deleteCombined(@PathVariable Long id) {
        log.debug("REST request to delete Combined : {}", id);
        combinedService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
