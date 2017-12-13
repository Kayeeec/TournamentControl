/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private final SetSettingsService setSettingsService;

    public TournamentService(UserRepository userRepository, TournamentRepository tournamentRepository, SetSettingsService setSettingsService) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
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
     * Tournaments that are not in Combined tournament.
     * @param participant
     * @return 
     */
    @Transactional(readOnly = true)
    public List<Tournament> findByParticipant(Participant participant) {
        log.debug("Request to get Tournaments with participant : {}", participant);
        List<Tournament> tournaments = tournamentRepository.findByParticipantsContainsAndInCombinedFalse(participant);
        return tournaments;
    }
    
    
    public void delete(Long id){
        Tournament toDelete = findOne(id);
        SetSettings setSettings = toDelete.getSetSettings();
        
        tournamentRepository.delete(id);
        
        //delete its setSettings if no other tournament has them
        List<Tournament> found_BySetSettings = findBySetSettings(setSettings);
        if(found_BySetSettings.size() == 0){
            setSettingsService.delete(setSettings.getId());
        }
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
    
    /**
     * Goes through matches sorted by round and extracts seeding.
     * seeding:
     *  [1A,2A,3A,3B,2B,1B]
     *    |  |  |  |  |  |
     *    |  |  +--+  |  |
     *    |  +--------+  |
     *    +--------------+
     * 
     * @param id - od a tournament
     * @return null if 1) id null - tournament to be created or no tournament found in db
     *                 2) tournament has no matches
     *         List<Participant> otherwise
     */
    public List<Participant> getSeeding(Long id) {
        if(id == null){ return null; }
        Tournament tournament = findOne(id);
        if(tournament == null){return null;}
        
        //get matches and sort them by round
        List<Game> matches = new ArrayList<>(tournament.getMatches());
        if(matches.isEmpty()) return null;
        Collections.sort(matches, Game.RoundComparator);
        
        int N = tournament.getParticipants().size() + tournament.getParticipants().size()%2;
        Participant[] seeding = new Participant[N];
        
        int a = 0;
        for (Game match : matches) {
            if(match.getRound() != 1){
                break;
            }            
            seeding[a]=match.getRivalA();
            seeding[N - 1 - a] = match.getRivalB();
            a += 1;
        }
        
        return Arrays.asList(seeding);
        
    }
    
    
    
    
    
    
    
    
}
