package cz.tournament.control.service;

import cz.tournament.control.domain.User;
import cz.tournament.control.domain.tournaments.Elimination;
import cz.tournament.control.repository.EliminationRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


/**
 * Service Implementation for managing Elimination.
 */
@Service
@Transactional
public class EliminationService {

    private final Logger log = LoggerFactory.getLogger(EliminationService.class);
    
    private final EliminationRepository eliminationRepository;
    private final UserRepository userRepository;

    public EliminationService(EliminationRepository eliminationRepository, UserRepository userRepository) {
        this.eliminationRepository = eliminationRepository;
        this.userRepository = userRepository;
    }
    
    public Elimination updateElimination(Elimination elimination){
        log.debug("Request to update Elimination : {}", elimination);
        
        /* TODO: Detect change and regenerate */
        
        Elimination result = eliminationRepository.save(elimination);
        log.debug("Elimination SERVICE: updated Elimination tournament: {}", result);

        return result;
        
    }
    
    public Elimination createElimination(Elimination elimination){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        elimination.setUser(creator);
        //set creation date
        elimination.setCreated(ZonedDateTime.now());
        
//        //generate assignment if it has participants        
//        Elimination tmp = eliminationRepository.save(elimination); //has to be in db before generating games
//        if(tmp.getParticipants().size() >= 2){
//            generateAssignment(tmp);
//            log.debug("Elimination SERVICE: generated matches: {}", tmp.getMatches());
//        }
        
        Elimination result = eliminationRepository.save(elimination);
        log.debug("Elimination SERVICE: created Elimination tournament: {}", result);
        return result;
        
        
    }


    /**
     * Save a elimination.
     *
     * @param elimination the entity to save
     * @return the persisted entity
     */
    public Elimination save(Elimination elimination) {
        log.debug("Request to save Elimination : {}", elimination);
        Elimination result = eliminationRepository.save(elimination);
        return result;
    }

    /**
     *  Get all the eliminations.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Elimination> findAll(Pageable pageable) {
        log.debug("Request to get all Eliminations");
        Page<Elimination> result = eliminationRepository.findByUserIsCurrentUser(pageable);
        return result;
    }

    /**
     *  Get one elimination by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Elimination findOne(Long id) {
        log.debug("Request to get Elimination : {}", id);
        Elimination elimination = eliminationRepository.findOne(id);
        return elimination;
    }

    /**
     *  Delete the  elimination by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Elimination : {}", id);
        eliminationRepository.delete(id);
    }
}
