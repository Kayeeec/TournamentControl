package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.service.EliminationService;
import cz.tournament.control.service.dto.EliminationDTO;
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
 * REST controller for managing Elimination.
 */
@RestController
@RequestMapping("/api")
public class EliminationResource {

    private final Logger log = LoggerFactory.getLogger(EliminationResource.class);

    private static final String ENTITY_NAME = "elimination";
        
    private final EliminationService eliminationService;

    public EliminationResource(EliminationService eliminationService) {
        this.eliminationService = eliminationService;
    }

//    /**
//     * POST  /eliminations : Create a new elimination.
//     *
//     * @param elimination the elimination to create
//     * @return the ResponseEntity with status 201 (Created) and with body the new elimination, or with status 400 (Bad Request) if the elimination has already an ID
//     * @throws URISyntaxException if the Location URI syntax is incorrect
//     */
//    @PostMapping("/eliminations")
//    @Timed
//    public ResponseEntity<Elimination> createElimination(@RequestBody Elimination elimination) throws URISyntaxException {
//        log.debug("REST request to save Elimination : {}", elimination);
//        if (elimination.getId() != null) {
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new elimination cannot already have an ID")).body(null);
//        }
//        Elimination result = eliminationService.createElimination(elimination);
//        return ResponseEntity.created(new URI("/api/eliminations/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
//            .body(result);
//    }
    
    /**
     * POST  /eliminations : Create a new elimination. with DTO
     *
     * @param elimination the elimination to create
     * @return the ResponseEntity with status 201 (Created) and with body the new elimination, or with status 400 (Bad Request) if the elimination has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/eliminations")
    @Timed
    public ResponseEntity<Elimination> createElimination(@RequestBody EliminationDTO eliminationDTO) throws URISyntaxException {
        
        log.debug("REST request to save EliminationDTO : {}", eliminationDTO);
        Elimination elimination = eliminationDTO.getElimination();
        if (elimination.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new elimination cannot already have an ID")).body(null);
        }
        Elimination result = eliminationService.createElimination(elimination, eliminationDTO.getSeeding());
        return ResponseEntity.created(new URI("/api/eliminations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    
    @PostMapping("/eliminations/recieveDTO")
    public void recieveEliminationDTO(@RequestBody EliminationDTO eliminationDTO){
        log.debug("Recieved {}, seedingIsEmpty: {}", eliminationDTO.toString(), eliminationDTO.getSeeding().isEmpty());
        
    }

//    /**
//     * PUT  /eliminations : Updates an existing elimination.
//     *
//     * @param elimination the elimination to update
//     * @return the ResponseEntity with status 200 (OK) and with body the updated elimination,
//     * or with status 400 (Bad Request) if the elimination is not valid,
//     * or with status 500 (Internal Server Error) if the elimination couldnt be updated
//     * @throws URISyntaxException if the Location URI syntax is incorrect
//     */
//    @PutMapping("/eliminations")
//    @Timed
//    public ResponseEntity<Elimination> updateElimination(@RequestBody Elimination elimination) throws URISyntaxException {
//        log.debug("REST request to update Elimination : {}", elimination);
//        if (elimination.getId() == null) {
//            return createElimination(elimination);
//        }
//        Elimination result = eliminationService.updateElimination(elimination);
//        return ResponseEntity.ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, elimination.getId().toString()))
//            .body(result);
//    }
    
    /**
     * PUT  /eliminations : Updates an existing elimination.
     *
     * @param elimination the elimination to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated elimination,
     * or with status 400 (Bad Request) if the elimination is not valid,
     * or with status 500 (Internal Server Error) if the elimination couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/eliminations")
    @Timed
    public ResponseEntity<Elimination> updateElimination(@RequestBody EliminationDTO eliminationDTO) throws URISyntaxException {
        log.debug("REST request to update EliminationDTO : {}", eliminationDTO);
        Elimination elimination = eliminationDTO.getElimination();
        if (elimination.getId() == null) {
            return createElimination(eliminationDTO);
        }
        Elimination result = eliminationService.updateElimination(elimination, eliminationDTO.getSeeding());
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, elimination.getId().toString()))
            .body(result);
    }
    
    @GetMapping("/eliminations/seeding/{id}")
    @Timed
    public List<Participant> getEliminationSeeding(@PathVariable Long id) {
        log.debug("REST request to get Elimination : {}", id);
        List<Participant> seeding = eliminationService.getEliminationSeeding(id);
        return seeding;
    }

    /**
     * GET  /eliminations : get all the eliminations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of eliminations in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/eliminations")
    @Timed
    public ResponseEntity<List<Elimination>> getAllEliminations(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Eliminations");
        Page<Elimination> page = eliminationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/eliminations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /eliminations/:id : get the "id" elimination.
     *
     * @param id the id of the elimination to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the elimination, or with status 404 (Not Found)
     */
    @GetMapping("/eliminations/{id}")
    @Timed
    public ResponseEntity<Elimination> getElimination(@PathVariable Long id) {
        log.debug("REST request to get Elimination : {}", id);
        Elimination elimination = eliminationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(elimination));
    }

    /**
     * DELETE  /eliminations/:id : delete the "id" elimination.
     *
     * @param id the id of the elimination to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/eliminations/{id}")
    @Timed
    public ResponseEntity<Void> deleteElimination(@PathVariable Long id) {
        log.debug("REST request to delete Elimination : {}", id);
        eliminationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
