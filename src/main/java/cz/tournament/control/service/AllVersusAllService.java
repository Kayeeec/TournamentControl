package cz.tournament.control.service;

import cz.tournament.control.domain.User;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.AllVersusAllRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing AllVersusAll.
 */
@Service
@Transactional
public class AllVersusAllService {

    private final Logger log = LoggerFactory.getLogger(AllVersusAllService.class);
    
    private final AllVersusAllRepository allVersusAllRepository;
    private final UserRepository userRepository;

    public AllVersusAllService(AllVersusAllRepository allVersusAllRepository, UserRepository userRepository) {
        this.allVersusAllRepository = allVersusAllRepository;
        this.userRepository = userRepository;
    }

    

    /**
     * Save a allVersusAll.
     *
     * @param allVersusAll the entity to save
     * @return the persisted entity
     */
    public AllVersusAll save(AllVersusAll allVersusAll) {
        log.debug("Request to save AllVersusAll : {}", allVersusAll);
        AllVersusAll result = allVersusAllRepository.save(allVersusAll);
        return result;
    }
    
    public AllVersusAll createAllVersusAll(AllVersusAll allVersusAll){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        allVersusAll.setUser(creator);
        //set creation date
        allVersusAll.setCreated(ZonedDateTime.now());
        
        AllVersusAll result = allVersusAllRepository.save(allVersusAll);
        log.debug("ALL-VERSUS-ALL_SERVICE: Created all-v-all Tournament: {}", result);
        return result;
        
        
    }

    /**
     *  Get all the allVersusAlls.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<AllVersusAll> findAll() {
        log.debug("Request to get all AllVersusAlls");
        List<AllVersusAll> result = allVersusAllRepository.findByUserIsCurrentUser();

        return result;
    }

    /**
     *  Get one allVersusAll by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public AllVersusAll findOne(Long id) {
        log.debug("Request to get AllVersusAll : {}", id);
        AllVersusAll allVersusAll = allVersusAllRepository.findOne(id);
        return allVersusAll;
    }

    /**
     *  Delete the  allVersusAll by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete AllVersusAll : {}", id);
        allVersusAllRepository.delete(id);
    }
}
