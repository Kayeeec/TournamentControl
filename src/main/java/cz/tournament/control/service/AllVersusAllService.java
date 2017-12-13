package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.AllVersusAllRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Service Implementation for managing AllVersusAll.
 */
@Service
@Transactional
public class AllVersusAllService {

    private final Logger log = LoggerFactory.getLogger(AllVersusAllService.class);
    
    private final AllVersusAllRepository allVersusAllRepository;
    private final UserRepository userRepository;
    private final GameService gameService;
    private final SetSettingsService setSettingsService;
    private final TournamentService tournamentService;

    public AllVersusAllService(AllVersusAllRepository allVersusAllRepository, UserRepository userRepository, GameService gameService, SetSettingsService setSettingsService, TournamentService tournamentService) {
        this.allVersusAllRepository = allVersusAllRepository;
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.setSettingsService = setSettingsService;
        this.tournamentService = tournamentService;
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
     * Updates allVersusAll entity. 
     * If participants or number of mutual matches has been changed, 
     * generates matches from scratch (old matches are deleted). 
     * 
     * @param allVersusAll entity to be updated
     * @return the updated/persisted entity 
     */
    public AllVersusAll updateAllVersusAll(AllVersusAll allVersusAll){
        log.debug("Request to update AllVersusAll : {}", allVersusAll);
        
        //detect change for new assignment generation       
        AllVersusAll old = allVersusAllRepository.findOne(allVersusAll.getId());
        
        if(!old.getParticipants().equals(allVersusAll.getParticipants()) 
                || !Objects.equals(old.getNumberOfMutualMatches(), allVersusAll.getNumberOfMutualMatches())
                || !Objects.equals(old.getPlayingFields(), allVersusAll.getPlayingFields())
                || !Objects.equals(old.getSetsToWin(), allVersusAll.getSetsToWin())){
            if(!allVersusAll.getMatches().isEmpty()) deleteAllMatches(allVersusAll);
            if(allVersusAll.getParticipants().size() >= 2) generateAssignment(allVersusAll);
        }
        
        AllVersusAll result = allVersusAllRepository.save(allVersusAll);
        log.debug("AllVersusAll SERVICE: updated AllVersusAll tournament: {}", result);

        return result;
        
    }
    
    /**
     * Creates new allVersusAll entity. 
     * Sets creation date, user and generates matches. 
     * 
     * @param allVersusAll entity to be created.    
     * @return created/persisted entity
     */
    public AllVersusAll createAllVersusAll(AllVersusAll allVersusAll){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        allVersusAll.setUser(creator);
        //set creation date
        allVersusAll.setCreated(ZonedDateTime.now());
        
        AllVersusAll tmp = allVersusAllRepository.save(allVersusAll); //has to be in db before generating games
        
        //generate assignment if it has participants
        if(tmp.getParticipants().size() >= 2){
            generateAssignment(tmp);
            log.debug("AllVersusAll SERVICE: generated matches: {}", tmp.getMatches());
        }
        
        AllVersusAll result = allVersusAllRepository.save(tmp);
        log.debug("AllVersusAll SERVICE: created AllVersusAll tournament: {}", result);
        return result;
        
        
    }

    /**
     *  Get all curent user's allVersusAll entities.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<AllVersusAll> findAll() {
        log.debug("Request to get all AllVersusAlls");
        List<AllVersusAll> result = allVersusAllRepository.findByUserIsCurrentUserAndInCombinedFalse();

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
        SetSettings setSettings = findOne(id).getSetSettings();
        allVersusAllRepository.delete(id);
        
        //delete its setSettings if no other tournament has them
        List<Tournament> found_BySetSettings = tournamentService.findBySetSettings(setSettings);
        if(found_BySetSettings.isEmpty()){
            setSettingsService.delete(setSettings.getId());
        }
    }
    public void delete(Collection<AllVersusAll> tournaments){
        //gather set settings 
        List<SetSettings> setSettingsList = new ArrayList<>();      
        for (Tournament tournament : tournaments) {
            setSettingsList.add(tournament.getSetSettings());
        }
        
        allVersusAllRepository.delete(tournaments);
        
        //delete orphaned setSettings
        while (!setSettingsList.isEmpty()) {            
            SetSettings setSettings = pop(setSettingsList);
            if(tournamentService.findBySetSettings(setSettings).isEmpty()){
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
    
    //delete all tournaments matches from tournament and database
    private void deleteAllMatches(AllVersusAll ava){
        List<Game> matches = new ArrayList<>(ava.getMatches());
        ava.setMatches(new HashSet<>());
        gameService.delete(matches);
    }
    
    //==========================================================================
    //Assignment generation 
    //==========================================================================
    
    private List<Participant> initCorrectListOfParticipants(AllVersusAll tournament){
         List<Participant> result = new ArrayList<>(tournament.getParticipants());
         if(result.size() % 2 == 1){
             result.add(null);
         }
         return result;
    }
    
    //generates match, rivals are on special positions in array, 
    private void generateMatches(int period, int r, List<Participant> participant, AllVersusAll tournament){
        int n = participant.size();
        Participant rivalA, rivalB;
        
        for (int i = 0; i < n/2; i++){
            rivalA = participant.get(i);
            rivalB = participant.get(n-1-i);
            if(rivalA != null && rivalB != null){
                Game match = new Game().period(period).tournament(tournament);
                if(period % 2 == 0){
                    match.rivalA(rivalB).rivalB(rivalA);
                }else{
                    match.rivalA(rivalA).rivalB(rivalB);
                }
                
                
                Game saved = gameService.createGame(match);
                tournament.addMatches(saved);
            }
        }
    }
    
    //shifts array of rivals, first is fixed
    private List<Participant> shift(List<Participant> participant){
        int n = participant.size();
        Participant[] result = new Participant[n];
        result[0] = participant.get(0);
        result[1] = participant.get(n-1);
        for (int i = 1; i < n-1; i++){
            result[i+1] = participant.get(i);
        }
        return Arrays.asList(result);
    }
    
    //generates Round-robin assignment for given tournament
    public void generateAssignment(AllVersusAll tournament){
         List<Participant> participant = initCorrectListOfParticipants(tournament);
         int n = participant.size();
         int shiftCount = n - 1;
         int periodCount = tournament.getNumberOfMutualMatches(); //readability
         
         for(int period = 1; period <= periodCount; period++){
             int round = 1, field = 1;
             for (int s = 1; s <= shiftCount; s++){
                Participant rivalA, rivalB;
                for (int i = 0; i < n/2; i++){
                    rivalA = participant.get(i);
                    rivalB = participant.get(n-1-i);
                    if(rivalA != null && rivalB != null){
                        Game match = new Game().period(period).tournament(tournament);
                        //switch rivals on different periods
                        if(period % 2 == 0){
                            match.rivalA(rivalB).rivalB(rivalA);
                        }else{
                            match.rivalA(rivalA).rivalB(rivalB);
                        }
                        //set field and round
                        if(tournament.getPlayingFields() != null && tournament.getPlayingFields() > 1){
                            match.round(round).playingField(field);
                            field += 1;
                            if(field > tournament.getPlayingFields() ){
                                field = 1;
                                round += 1;
                            }
                        }else { match.round(s).playingField(field);}

                        Game saved = gameService.createGame(match);
                        tournament.addMatches(saved);
                    }
                }   
                participant = shift(participant);
             }
         }
         
    }
    
}
