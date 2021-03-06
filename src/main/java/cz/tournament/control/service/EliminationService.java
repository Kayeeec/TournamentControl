package cz.tournament.control.service;

import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.User;
import cz.tournament.control.domain.enumeration.EliminationType;
import cz.tournament.control.repository.EliminationRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Elimination.
 */
@Service
@Transactional
public class EliminationService {

    private final Logger log = LoggerFactory.getLogger(EliminationService.class);
    
    private final EliminationRepository eliminationRepository;
    private final UserRepository userRepository;
    private final GameService gameService;
    private final SetSettingsService setSettingsService;
    private final ParticipantService participantService;
    private final TournamentService tournamentService;

    public EliminationService(EliminationRepository eliminationRepository, UserRepository userRepository, GameService gameService, SetSettingsService setSettingsService, ParticipantService participantService, TournamentService tournamentService) {
        this.eliminationRepository = eliminationRepository;
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.setSettingsService = setSettingsService;
        this.participantService = participantService;
        this.tournamentService = tournamentService;
    }
    
    public Elimination updateElimination(Elimination elimination, List<Participant> seeding) {
        log.debug("Request to update Elimination : {}", elimination);

        /* Detect change and regenerate */
        Elimination old = eliminationRepository.findOne(elimination.getId());
        if (!Objects.equals(old.getType(), elimination.getType())
                || !Objects.equals(old.getBronzeMatch(), elimination.getBronzeMatch())
                || !Objects.equals(old.getSetsToWin(), elimination.getSetsToWin())
                || !Objects.equals(old.getParticipants(), elimination.getParticipants()) 
                || !Objects.equals(getEliminationSeeding(old.getId()), seeding)) {
            
            if (!elimination.getMatches().isEmpty()) {
                deleteAllMatches(elimination);
                elimination = eliminationRepository.save(elimination);
            }
            if (elimination.getParticipants().size() >= 2) {
                generateAssignment(elimination, seeding);
            }
        }

        Elimination result = eliminationRepository.save(elimination);
        log.debug("Elimination SERVICE: updated Elimination tournament: {}", result);

        return result;

    }
    
    //delete all tournaments matches from tournament and database
    private void deleteAllMatches(Elimination elimination){
        List<Game> matches = new ArrayList<>(elimination.getMatches());
        elimination.setMatches(new HashSet<>());
        gameService.delete(matches);
    }
    
    public Elimination createElimination(Elimination elimination, List<Participant> seeding){
        //set creator as user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        elimination.setUser(creator);
        //set creation date
        elimination.setCreated(ZonedDateTime.now());
        //set tiesAllowed
        elimination.tiesAllowed(Boolean.FALSE);
        
        //generate assignment if it has participants        
        Elimination tmp = eliminationRepository.save(elimination); //has to be in db before generating games
        if(tmp.getParticipants().size() >= 2){
            generateAssignment(tmp, seeding);
            //log.debug("Elimination SERVICE: generated matches: {}", tmp.getMatches());
        }
        
        Elimination result = eliminationRepository.save(tmp);
        log.debug("Elimination SERVICE: created Elimination tournament: {}", result);
        return result;
        
        
    }


    /**
     * Save a elimination.
     *
     * @param elimination the entity to save
     * @return the persisted entity
     */
    public Elimination save(Elimination elimination) {
        log.debug("Request to save Elimination : {}", elimination);
        Elimination result = eliminationRepository.save(elimination);
        return result;
    }
    
    public List<Participant> getEliminationSeeding(Long id){
        if(id == null){ return null; }
        Elimination elimination = findOne(id);
        if(elimination == null){return null;}
        int N = elimination.getN();
        List<Game> matches = new ArrayList<>(elimination.getMatches());
        
        //sort matches by period
        Collections.sort(matches, Game.PeriodRoundComparator);
        
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

    /**
     *  Get all the eliminations.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Elimination> findAll(Pageable pageable) {
        log.debug("Request to get all Eliminations");
        Page<Elimination> result = eliminationRepository.findByUserIsCurrentUserAndInCombinedFalse(pageable);
        return result;
    }

    /**
     *  Get one elimination by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Elimination findOne(Long id) {
        log.debug("Request to get Elimination : {}", id);
        Elimination elimination = eliminationRepository.findOne(id);
        return elimination;
    }

    /**
     *  Delete the  elimination by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Elimination : {}", id);
        SetSettings setSettings = findOne(id).getSetSettings();
        eliminationRepository.delete(id);
        
        //delete its setSettings if no other tournament has them
        List<Tournament> found_BySetSettings = tournamentService.findBySetSettings(setSettings);
        if(found_BySetSettings.isEmpty()){
            setSettingsService.delete(setSettings.getId());
        }
        
        
    }
    
    public void delete(Collection<Elimination> tournaments) {
        log.debug("Request to delete Eliminations : {}", tournaments);
        
        //gather set settings 
        List<SetSettings> setSettingsList = new ArrayList<>();      
        for (Tournament tournament : tournaments) {
            setSettingsList.add(tournament.getSetSettings());
        }
        
        eliminationRepository.delete(tournaments);
        
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
    
    
    public void generateAssignment(Elimination tournament, List<Participant> seeding){
        if(tournament.getType().equals(EliminationType.SINGLE)){
            generateSingle(tournament, seeding);
        }
        if(tournament.getType().equals(EliminationType.DOUBLE)){
            generateDouble(tournament, seeding);
        }
    }
    
    private List<Participant> initParticipants(Set<Participant> participants, int N, List<Participant> seeding){
        if(seeding != null && !seeding.contains(null) && seeding.size() == N){
            return seeding;
        }
        int n = participants.size();
        if(n == N){
            return new ArrayList<>(participants);
        }
        int byes = N - n;
        Participant bye = participantService.getByeParticipant();
        
        List<Participant> result = new ArrayList<>(participants);
        for (int i = 0; i < byes; i++) {
            result.add(bye);
        }
        return result;
    }
    
    /**
     * Lets talk numbers: SINGLE
     * ~~~~~~~~~~~~~~~~~~
     * n - number of participants
     * N - effective number of participants (includes BYE) and matches
     *     N = the closest power of two bigger or equal n
     * #BYE = N - n
     * #rounds = log_2(N)
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Orientation in an array of matches ordered by round -> period 
     *      (or id depending on implementation)
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * root - index of a match deciding the winner
     *      root = N - 2
     * bronze - index of a match deciding the 3rd place 
     *      bronze = N - 1
     * nextMatchIndex(index) = floor((index + root)/2)+1
     *      - index - of a match we look for next match to
     *      - floor - rounding down the division
     */
    
    /**
     * "Populate first round" part might need to be looked at.
     * @param tournament 
     */
    public void generateSingle(Elimination tournament, List<Participant> seeding){
        int N = tournament.getN();
        int rounds = (int) (Math.log(N) / Math.log(2));
        List<Game> roundOne = new ArrayList<>();
        
        //generate all matches, set round and period (period for ordering)
        int period = 1;
        for (int round = 1; round <= rounds; round++) {
            for (int i = 0; i < (N/(Math.pow(2, round))); i++) {
                Game saved = gameService.createGame(new Game().tournament(tournament).round(round).period(period));
                tournament.addMatches(saved);
                period += 1;
                if(round == 1){
                    roundOne.add(saved);
                }
            }
        }
        //3rd place match
        if(tournament.getBronzeMatch()==true){
            Game bronzeMatch = gameService.createGame(new Game().tournament(tournament).round(rounds).period(period));
            tournament.addMatches(bronzeMatch);  
        }
        //populate first round, set BYE matches as finished 
        //random
        List<Participant> participants = initParticipants(tournament.getParticipants(), N, seeding);
        int first = 0;
        int last = participants.size() - 1;
        for (Game match : roundOne) {
            match.rivalA(participants.get(first)).rivalB(participants.get(last));
            first += 1;
            last -= 1;
            if(match.getRivalA().isBye() || match.getRivalB().isBye()){
                match.setFinished(Boolean.TRUE);
            }
            gameService.updateGame(match);
        }
        
    }
    
    /**
     * Lets talk numbers: DOUBLE
     * ~~~~~~~~~~~~~~~~~~
     * n - number of participants
     * N - effective number of participants (includes BYE)
     *     N = the closest power of two bigger or equal n
     * #BYE = N - n
     * #winnerRounds = log_2(N)
     * #rounds = log_2(N) + 1
     *  
     * numbering rounds in loser bracket: 15..log_2(N), incremented by 5, NEGATIVE!!!
     * #loserBracketMatches = N - 2
     * #total matches = (N - 1) + (N - 2) + 1 {final} [+ 1 {if bronze}] [+1 {if another final}]
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Orientation in an array of matches ordered by round -> period 
     *      (or id depending on implementation)
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * root - index of a match deciding the winner
     *      root = 2N - 3
     * additional root - index of a possible next root
     *      additionalRoot = 2N-1 [if bronze] or 2N-2 
     * bronze - index of a match deciding the 3rd place 
     *      bronze = 2N - 2
     * loserSegmentSize(round) = N/{2^[ceiling(R/10)]}
     * loserInSegmentIndex(index, round) = {index [+1 if odd] % loserSegmentSize(round)}/2
     * loserNextMatchIndex(index, round) = index [-1 if even] + loserSegmentSize(round) - loserInSegmentIndex(index, round);
     * 
     * nextMatchIndex(index) = floor((index + root)/2)+1
     *      - index - of a match we look for next match to
     *      - floor - rounding down the division
     */
    
    /**
     * "Populate first round" part might need to be loodek at.
     * @param tournament 
     */
    public void generateDouble(Elimination tournament, List<Participant> seeding){
        //generate winner bracket matches and populate first round
        int N = tournament.getN();
        int winnerRounds = (int) (Math.log(N) / Math.log(2));
        List<Game> roundOne = new ArrayList<>();
        
        //generate all winner matches, set round and period (period for ordering)
        int period = 1;
        for (int round = 1; round <= winnerRounds; round++) {
            for (int i = 0; i < (N/(Math.pow(2, round))); i++) {
                Game saved = gameService.createGame(new Game().tournament(tournament).round(round).period(period));
                tournament.addMatches(saved);
                period += 1;
                if(round == 1){
                    roundOne.add(saved);
                }
            }
        }
        //generate loser bracket matches
        for(int round = -15; round >= winnerRounds*(-10); round -= 5){
            for (int i = 0; i < loserSegmentSize(round, N); i++) {
                Game saved = gameService.createGame(new Game().tournament(tournament).round(round).period(period));
                tournament.addMatches(saved);
                period += 1;
            }
        }
        //generate final match and bronze match
        Game finalMatch = gameService.createGame(new Game().tournament(tournament).round(winnerRounds + 1).period(period));
        tournament.addMatches(finalMatch);
        period += 1;
        if(tournament.getBronzeMatch()==true){
            Game bronzeMatch = gameService.createGame(new Game().tournament(tournament).round(winnerRounds + 1).period(period));
            tournament.addMatches(bronzeMatch);  
        }
        
        //populate first round, set BYE matches as finished 
        //random
        List<Participant> participants = initParticipants(tournament.getParticipants(), N, seeding);
        int first = 0;
        int last = participants.size() - 1;
        for (Game match : roundOne) {
            match.rivalA(participants.get(first)).rivalB(participants.get(last));
            first += 1;
            last -= 1;
            if(match.getRivalA().isBye() || match.getRivalB().isBye()){ 
                match.setFinished(Boolean.TRUE);
            }
            gameService.updateGame(match);
        }
    }

    /**
     * loserSegmentSize(round) = N/{2^[ceiling(R/10)]}
     * 
     * @param round
     * @param N
     * @return 
     */
    private int loserSegmentSize(int round, int N) {
        int loserSegmentSize = (int) (N/(Math.pow(2, Math.ceil((double)(-1)*round/10))));
        return loserSegmentSize;
    }

    
}
