package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.AllVersusAll;
import cz.tournament.control.service.AllVersusAllService;
import cz.tournament.control.web.rest.errors.BadRequestAlertException;
import cz.tournament.control.web.rest.util.HeaderUtil;
import cz.tournament.control.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing AllVersusAll.
 */
@RestController
@RequestMapping("/api")
public class AllVersusAllResource {

    private final Logger log = LoggerFactory.getLogger(AllVersusAllResource.class);

    private static final String ENTITY_NAME = "allVersusAll";

    private final AllVersusAllService allVersusAllService;

    public AllVersusAllResource(AllVersusAllService allVersusAllService) {
        this.allVersusAllService = allVersusAllService;
    }

    /**
     * POST  /all-versus-alls : Create a new allVersusAll.
     *
     * @param allVersusAll the allVersusAll to create
     * @return the ResponseEntity with status 201 (Created) and with body the new allVersusAll, or with status 400 (Bad Request) if the allVersusAll has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/all-versus-alls")
    @Timed
    public ResponseEntity<AllVersusAll> createAllVersusAll(@Valid @RequestBody AllVersusAll allVersusAll) throws URISyntaxException {
        log.debug("REST request to save AllVersusAll : {}", allVersusAll);
        if (allVersusAll.getId() != null) {
            throw new BadRequestAlertException("A new allVersusAll cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AllVersusAll result = allVersusAllService.createAllVersusAll(allVersusAll);
        
        return ResponseEntity.created(new URI("/api/all-versus-alls/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /all-versus-alls : Updates an existing allVersusAll.
     *
     * @param allVersusAll the allVersusAll to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated allVersusAll,
     * or with status 400 (Bad Request) if the allVersusAll is not valid,
     * or with status 500 (Internal Server Error) if the allVersusAll couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/all-versus-alls")
    @Timed
    public ResponseEntity<AllVersusAll> updateAllVersusAll(@Valid @RequestBody AllVersusAll allVersusAll) throws URISyntaxException {
        log.debug("REST request to update AllVersusAll : {}", allVersusAll);
        if (allVersusAll.getId() == null) {
            return createAllVersusAll(allVersusAll);
        }
        AllVersusAll result = allVersusAllService.updateAllVersusAll(allVersusAll);
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, allVersusAll.getId().toString()))
            .body(result);
    }

    /**
     * GET  /all-versus-alls : get all the allVersusAlls.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of allVersusAlls in body
     */
    @GetMapping("/all-versus-alls")
    @Timed
    public ResponseEntity<List<AllVersusAll>> getAllAllVersusAlls(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of AllVersusAlls");
        Page<AllVersusAll> page = allVersusAllService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/all-versus-alls");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /all-versus-alls/:id : get the "id" allVersusAll.
     *
     * @param id the id of the allVersusAll to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the allVersusAll, or with status 404 (Not Found)
     */
    @GetMapping("/all-versus-alls/{id}")
    @Timed
    public ResponseEntity<AllVersusAll> getAllVersusAll(@PathVariable Long id) {
        log.debug("REST request to get AllVersusAll : {}", id);
        AllVersusAll allVersusAll = allVersusAllService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(allVersusAll));
    }

    /**
     * DELETE  /all-versus-alls/:id : delete the "id" allVersusAll.
     *
     * @param id the id of the allVersusAll to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/all-versus-alls/{id}")
    @Timed
    public ResponseEntity<Void> deleteAllVersusAll(@PathVariable Long id) {
        log.debug("REST request to delete AllVersusAll : {}", id);
        allVersusAllService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
