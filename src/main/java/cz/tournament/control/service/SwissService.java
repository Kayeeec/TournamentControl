package cz.tournament.control.service;

import cz.tournament.control.domain.Swiss;
import cz.tournament.control.repository.SwissRepository;
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

    public SwissService(SwissRepository swissRepository) {
        this.swissRepository = swissRepository;
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
