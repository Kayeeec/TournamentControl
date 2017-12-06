/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import aj.org.objectweb.asm.Opcodes;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.AllVersusAllRepository;
import cz.tournament.control.repository.EliminationRepository;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Karolina Bozkova
 */
@Service
@Transactional
public class TournamentService {
    private final Logger log = LoggerFactory.getLogger(TournamentService.class);

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final EliminationRepository eliminationRepository;
    private final AllVersusAllRepository allVersusAllRepository;
    private final SetSettingsService setSettingsService;

    public TournamentService(UserRepository userRepository, TournamentRepository tournamentRepository, EliminationRepository eliminationRepository, AllVersusAllRepository allVersusAllRepository, SetSettingsService setSettingsService) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
        this.eliminationRepository = eliminationRepository;
        this.allVersusAllRepository = allVersusAllRepository;
        this.setSettingsService = setSettingsService;
    }
    
    public Tournament createTournament(Tournament tournament){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        tournament.setUser(creator);
        //set creation date
        tournament.setCreated(ZonedDateTime.now());
        
        Tournament result = tournamentRepository.save(tournament);
        log.debug("TOUTNAMENT_SERVICE: Created Tournament: {}", result);
        return result;   
    }
    
    public Tournament updateTournament(Tournament tournament){
        Tournament result = tournamentRepository.save(tournament);
        return result;
    }
    
    public List<Tournament> findAll(){
        List<Tournament> tournaments = tournamentRepository.findByUserIsCurrentUser();
        return tournaments;
    }
    
    /**
     *  Get one game by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Tournament findOne(Long id) {
        log.debug("Request to get Tournament : {}", id);
        Tournament tournament = tournamentRepository.findOneWithEagerRelationships(id);
        return tournament;

//        Tournament tournament = eliminationRepository.findOne(id);
//        if(tournament != null) return tournament;
//        tournament = allVersusAllRepository.findOne(id);
//        if(tournament != null) return tournament;
//        //none of the above
//        return tournamentRepository.findOneWithEagerRelationships(id);
    }
    
    /**
     *  Get all tournaments with given SetSettings entity.
     *
     *  @param setSettings entity
     *  @return list of Tournament entities
     */
    @Transactional(readOnly = true)
    public List<Tournament> findBySetSettings(SetSettings setSettings) {
        log.debug("Request to get Tournaments with setSettings : {}", setSettings);
        List<Tournament> tournaments = tournamentRepository.findBySetSettings(setSettings);
        return tournaments;
    }
    
    /**
     * Finds tournaments with given participant.
     * @param participant
     * @return 
     */
    @Transactional(readOnly = true)
    public List<Tournament> findByParticipant(Participant participant) {
        log.debug("Request to get Tournaments with participant : {}", participant);
        List<Tournament> tournaments = tournamentRepository.findByParticipantsContains(participant);
        return tournaments;
    }
    
    
    public void delete(Long id){
        //delete its setSettings if no other tournament has them
        SetSettings setSettings = findOne(id).getSetSettings();
        List<Tournament> found_BySetSettings = findBySetSettings(setSettings);
        if(found_BySetSettings.size() == 1){
            setSettingsService.delete(setSettings.getId());
        }
        
        tournamentRepository.delete(id);
    }
    
    public void delete(Collection<Tournament> tournaments){
        //gather set settings 
        List<SetSettings> setSettingsList = new ArrayList<>();      
        for (Tournament tournament : tournaments) {
            setSettingsList.add(tournament.getSetSettings());
        }
        //delete tournaments
        tournamentRepository.delete(tournaments);
        
        //delete orphaned setSettings
        while (!setSettingsList.isEmpty()) {            
            SetSettings setSettings = pop(setSettingsList);
            if(findBySetSettings(setSettings).isEmpty()){
                setSettingsService.delete(setSettings.getId());
            }
        }
    }
    
    /**
     * removes element from list and returns it 
     * 
     * @param list
     * @return null if list is empty or null, SetSettings entity otherwise
     */
    private static SetSettings pop(List<SetSettings> list) {
        if(list == null || list.isEmpty()) return null;
        int index = list.size() - 1;
        SetSettings result = list.get(index);
        list.remove(index);
        return result;
    }
    
    
    
    
    
    
    
    
}
