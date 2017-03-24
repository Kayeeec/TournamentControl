package cz.tournament.control.service;

import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.AllVersusAllRepository;
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

    public AllVersusAllService(AllVersusAllRepository allVersusAllRepository) {
        this.allVersusAllRepository = allVersusAllRepository;
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

    /**
     *  Get all the allVersusAlls.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<AllVersusAll> findAll() {
        log.debug("Request to get all AllVersusAlls");
        List<AllVersusAll> result = allVersusAllRepository.findAll();

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
