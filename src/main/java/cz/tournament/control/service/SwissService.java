package cz.tournament.control.service;

import cz.tournament.control.domain.Swiss;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.SwissRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Swiss.
 */
@Service
@Transactional
public class SwissService {

    private final Logger log = LoggerFactory.getLogger(SwissService.class);

    private final SwissRepository swissRepository;
    private final UserRepository userRepository;
    private final SetSettingsService setSettingsService;

    public SwissService(SwissRepository swissRepository, UserRepository userRepository, SetSettingsService setSettingsService) {
        this.swissRepository = swissRepository;
        this.userRepository = userRepository;
        this.setSettingsService = setSettingsService;
    }

   

    /**
     * Save a swiss.
     *
     * @param swiss the entity to save
     * @return the persisted entity
     */
    public Swiss save(Swiss swiss) {
        log.debug("Request to save Swiss : {}", swiss);
        return swissRepository.save(swiss);
    }
    
    /**
     *
     * @param swiss
     * @return
     */
    public Swiss createSwiss(Swiss swiss){
        log.debug("Request to create Swiss : {}", swiss);
        //set user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        swiss.setUser(creator);
        //set creation date
        swiss.setCreated(ZonedDateTime.now());
        
        Swiss tmp = swissRepository.save(swiss); //has to be in db before generating games
        
        /*
        if it has more than 2 participants
        generating games here / todo
        */
        
        return tmp;
    }
    
    /**
     *
     * @param swiss
     * @return
     */
    public Swiss updateSwiss(Swiss swiss){
        log.debug("Request to update Swiss : {}", swiss);
        
        //get old one from db
        //detect change
        /*
        update/regenerate games if necesary 
        */
        
        return swissRepository.save(swiss);
       
    }

    /**
     *  Get all the swisses.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Swiss> findAll(Pageable pageable) {
        log.debug("Request to get all Swisses");
        return swissRepository.findByUserIsCurrentUser(pageable);
    }

    /**
     *  Get one swiss by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Swiss findOne(Long id) {
        log.debug("Request to get Swiss : {}", id);
        return swissRepository.findOne(id);
    }

    /**
     *  Delete the  swiss by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Swiss : {}", id);
        swissRepository.delete(id);
    }
}
