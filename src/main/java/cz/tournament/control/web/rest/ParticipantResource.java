package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Participant;

import cz.tournament.control.repository.ParticipantRepository;
import cz.tournament.control.service.ParticipantService;
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
 * REST controller for managing Participant.
 */
@RestController
@RequestMapping("/api")
public class ParticipantResource {

    private final Logger log = LoggerFactory.getLogger(ParticipantResource.class);

    private static final String ENTITY_NAME = "participant";
        
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;

    public ParticipantResource(ParticipantRepository participantRepository, ParticipantService participantService) {
        this.participantRepository = participantRepository;
        this.participantService = participantService;
    }

    /**
     * POST  /participants : Create a new participant.
     *
     * @param participant the participant to create
     * @return the ResponseEntity with status 201 (Created) and with body the new participant, or with status 400 (Bad Request) if the participant has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/participants")
    @Timed
    public ResponseEntity<Participant> createParticipant(@Valid @RequestBody Participant participant) throws URISyntaxException {
        log.debug("REST request to save Participant : {}", participant);
        if (participant.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new participant cannot already have an ID")).body(null);
        }
        Participant result = participantRepository.save(participant);
        return ResponseEntity.created(new URI("/api/participants/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /participants : Updates an existing participant.
     *
     * @param participant the participant to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated participant,
     * or with status 400 (Bad Request) if the participant is not valid,
     * or with status 500 (Internal Server Error) if the participant couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/participants")
    @Timed
    public ResponseEntity<Participant> updateParticipant(@Valid @RequestBody Participant participant) throws URISyntaxException {
        log.debug("REST request to update Participant : {}", participant);
        if (participant.getId() == null) {
            return createParticipant(participant);
        }
        Participant result = participantRepository.save(participant);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, participant.getId().toString()))
            .body(result);
    }

    /**
     * GET  /participants : get all the participants.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of participants in body
     */
    @GetMapping("/participants")
    @Timed
    public List<Participant> getAllParticipants() {
        log.debug("REST request to get all Participants");
        List<Participant> participants = participantRepository.findByUserIsCurrentUser();
        log.debug("    participants: {}", participants);
        return participants;
    }

    /**
     * GET  /participants/:id : get the "id" participant.
     *
     * @param id the id of the participant to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the participant, or with status 404 (Not Found)
     */
    @GetMapping("/participants/{id}")
    @Timed
    public ResponseEntity<Participant> getParticipant(@PathVariable Long id) {
        log.debug("REST request to get Participant : {}", id);
        Participant participant = participantRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(participant));
    }
    
    /**
     * Finds or creates and returns BYE participant. Used in Elimination, for seeding.
     * @return  BYE participant, has no team nor player, for every user only one in database.
     */
    @GetMapping("/participants/bye")
    @Timed
    public ResponseEntity<Participant> getByeParticipant() {
        log.debug("REST request to get BYE [articipant");
        Participant participant = participantService.getByeParticipant();
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(participant));
    }

    /**
     * DELETE  /participants/:id : delete the "id" participant.
     *
     * @param id the id of the participant to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/participants/{id}")
    @Timed
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long id) {
        log.debug("REST request to delete Participant : {}", id);
        participantRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
