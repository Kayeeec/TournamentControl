package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.User;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.AllVersusAllRepository;
import cz.tournament.control.repository.GameRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    private final GameRepository gameRepository;

    public AllVersusAllService(AllVersusAllRepository allVersusAllRepository, UserRepository userRepository, GameRepository gameRepository) {
        this.allVersusAllRepository = allVersusAllRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
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
    
    public AllVersusAll updateAllVersusAll(AllVersusAll allVersusAll){
        log.debug("Request to update AllVersusAll : {}", allVersusAll);
        
        //generate assignment if it has participants
        if(!allVersusAll.getParticipants().isEmpty()){
            if(!allVersusAll.getMatches().isEmpty()) deleteAllMatches(allVersusAll);
            generateAssignment(allVersusAll);
        }
        
        AllVersusAll result = allVersusAllRepository.save(allVersusAll);
        log.debug("AllVersusAll SERVICE: updated AllVersusAll tournament: {}", result);

        return result;
        
    }
    
    public AllVersusAll createAllVersusAll(AllVersusAll allVersusAll){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        allVersusAll.setUser(creator);
        //set creation date
        allVersusAll.setCreated(ZonedDateTime.now());
        //generate assignment if it has participants
        if(!allVersusAll.getParticipants().isEmpty()){
            generateAssignment(allVersusAll);
            log.debug("AllVersusAll SERVICE: generated matches: {}", allVersusAll.getMatches());
        }
        
        
        AllVersusAll result = allVersusAllRepository.save(allVersusAll);
        log.debug("AllVersusAll SERVICE: created AllVersusAll tournament: {}", result);
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
        
        //delete all tournaments matches from tournament and database
        deleteAllMatches(allVersusAllRepository.findOne(id));
        
        allVersusAllRepository.delete(id);
    }
    
    //delete all tournaments matches from tournament and database
    private void deleteAllMatches(AllVersusAll ava){
        for (Game match : ava.getMatches()) {
            ava.removeMatches(match);
            gameRepository.delete(match.getId());
        }
    }
    
    //==========================================================================
    //Assignment generation 
    //==========================================================================
    private int getNumberOfRounds(AllVersusAll allVersusAll) {
        //při sudém počtu týmů je počet kol N–1, při lichém počtu je počet kol roven počtu týmů N
       int n = allVersusAll.getParticipants().size();
       if (n % 2 == 0) return n-1;
       return n;
    }
    
    private Integer[] assignmentInit(int n){
        if(n%2==1){
            Integer[] result = new Integer[n + 1];
            for (int i = 0; i < n; i++){
                result[i]=i;
            }
            result[n] = null;
            return result;
        }
        Integer[] result = new Integer[n];
        for (int i = 0; i < n; i++){
            result[i]=i;
        }
        return result;
    }
    
    private void generateMatches(List<Participant> arr, Integer[] index, int round, int period, AllVersusAll allVersusAll){
        int n = index.length;
        for (int i = 0; i <= n/2; i++){
            if (index[i] != null && index[n-1-i] != null){  
                Game match = gameRepository.save(new Game().tournament(allVersusAll)
                        .period(period)
                        .round(round)
                        .rivalA(arr.get(index[i]))
                        .rivalB(arr.get(index[n-1-i])));                
                allVersusAll.addMatches(match);  
            }  
        }
    }
    
    private Integer[] shiftIndices(Integer[] index){
        int n = index.length;
        Integer[] result = new Integer[n];
        
        for (int i = 1; i <= n-2; i++){
            result[i+1]=index[i];
        }
        result[1]=index[n-1];
        
        return result;
    }
    
    public void generateAssignment(AllVersusAll allVersusAll){
        int numberOfPeriods = allVersusAll.getNumberOfMutualMatches();
        
        List<Participant> arr = new ArrayList<>(allVersusAll.getParticipants());
        Integer[] index = assignmentInit(arr.size());
        
        for (int period = 1; period <= numberOfPeriods; period++) {
            for (int round = 1; round <= getNumberOfRounds(allVersusAll); round++){
                generateMatches(arr, index, round, period, allVersusAll);
                index = shiftIndices(index);
            }
        }  
    }
}
