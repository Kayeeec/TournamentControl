package cz.tournament.control.service;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.repository.CombinedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Combined.
 */
@Service
@Transactional
public class CombinedService {

    private final Logger log = LoggerFactory.getLogger(CombinedService.class);

    private final CombinedRepository combinedRepository;

    public CombinedService(CombinedRepository combinedRepository) {
        this.combinedRepository = combinedRepository;
    }

    /**
     * Save a combined.
     *
     * @param combined the entity to save
     * @return the persisted entity
     */
    public Combined save(Combined combined) {
        log.debug("Request to save Combined : {}", combined);
        return combinedRepository.save(combined);
    }

    /**
     *  Get all the combineds.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Combined> findAll(Pageable pageable) {
        log.debug("Request to get all Combineds");
        return combinedRepository.findByUserIsCurrentUser(pageable);
    }

    /**
     *  Get one combined by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Combined findOne(Long id) {
        log.debug("Request to get Combined : {}", id);
        return combinedRepository.findOne(id);
    }

    /**
     *  Delete the  combined by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Combined : {}", id);
        combinedRepository.delete(id);
    }
}
