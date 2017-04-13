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
import java.util.Arrays;
import java.util.HashSet;
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
    private final GameService gameService;

    public AllVersusAllService(AllVersusAllRepository allVersusAllRepository, UserRepository userRepository, GameRepository gameRepository, GameService gameService) {
        this.allVersusAllRepository = allVersusAllRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
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
        
        AllVersusAll old = allVersusAllRepository.findOne(allVersusAll.getId());
        if(!old.getParticipants().equals(allVersusAll.getParticipants()) 
                || old.getNumberOfMutualMatches() != allVersusAll.getNumberOfMutualMatches()){
            if(!allVersusAll.getMatches().isEmpty()) deleteAllMatches(allVersusAll);
            if(allVersusAll.getParticipants().size() >= 2) generateAssignment(allVersusAll);
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
    
    private void generateMatches(int period, int round, List<Participant> participant, AllVersusAll tournament){
        int n = participant.size();
        Participant rivalA, rivalB;
        for (int i = 0; i < n/2; i++){
            rivalA = participant.get(i);
            rivalB = participant.get(n-1-i);
            if(rivalA != null && rivalB != null){
                Game match;
                if(period % 2 == 0){
                    match = new Game().period(period).round(round).rivalA(rivalB).rivalB(rivalA).tournament(tournament);
                }else{
                    match = new Game().period(period).round(round).rivalA(rivalA).rivalB(rivalB).tournament(tournament);
                }
                Game saved = gameService.createGame(match);
                tournament.addMatches(saved);
            }
        }
    }
    
    
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
    
    public void generateAssignment(AllVersusAll tournament){
         List<Participant> participant = initCorrectListOfParticipants(tournament);
         int roundCount = participant.size() - 1;
         int periodCount = tournament.getNumberOfMutualMatches(); //readability
         
         for(int p = 1; p <= periodCount; p++){
             for (int r = 1; r <= roundCount; r++){
                 log.debug("p: {}, r: {}, working list: {}", p,r,participant);
                 generateMatches(p, r, participant, tournament);
                 participant = shift(participant);
             }
         }
         
    }
    
//    public static List<String> shiftStrings(List<String> participant){
//        int n = participant.size();
//        String[] result = new String[n];
//        result[0] = participant.get(0);
//        result[1] = participant.get(n-1);
//        for (int i = 1; i < n-1; i++){
//            result[i+1] = participant.get(i);
//        }
//        return Arrays.asList(result);
//    }
//    
//    public static void main(String[] args) {
//        System.out.println("Hello");
//        List<String> list = new ArrayList<>();
//        list.add(new String("Dana"));
//        list.add(new String("Bety"));
//        System.out.println(list);
//        System.out.println("size = " + list.size());
//        System.out.println("list after shift:");
//        list = shiftStrings(list);
//        System.out.println(list);
//        
//        
//    }
}
