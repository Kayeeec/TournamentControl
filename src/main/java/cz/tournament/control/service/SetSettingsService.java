package cz.tournament.control.service;

import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.repository.SetSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing SetSettings.
 */
@Service
@Transactional
public class SetSettingsService {

    private final Logger log = LoggerFactory.getLogger(SetSettingsService.class);
    
    private final SetSettingsRepository setSettingsRepository;

    public SetSettingsService(SetSettingsRepository setSettingsRepository) {
        this.setSettingsRepository = setSettingsRepository;
    }

    /**
     * Save a setSettings.
     *
     * @param setSettings the entity to save
     * @return the persisted entity
     */
    public SetSettings save(SetSettings setSettings) {
        log.debug("Request to save SetSettings : {}", setSettings);
        SetSettings result = setSettingsRepository.save(setSettings);
        return result;
    }

    /**
     *  Get all the setSettings.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SetSettings> findAll() {
        log.debug("Request to get all SetSettings");
        List<SetSettings> result = setSettingsRepository.findAll();

        return result;
    }

    /**
     *  Get one setSettings by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public SetSettings findOne(Long id) {
        log.debug("Request to get SetSettings : {}", id);
        SetSettings setSettings = setSettingsRepository.findOne(id);
        return setSettings;
    }

    /**
     *  Delete the  setSettings by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete SetSettings : {}", id);
        setSettingsRepository.delete(id);
    }
}
