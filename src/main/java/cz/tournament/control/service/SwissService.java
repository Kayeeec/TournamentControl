package cz.tournament.control.service;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.domain.User;
import cz.tournament.control.domain.exceptions.RunPythonScriptException;
import cz.tournament.control.repository.SwissRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import cz.tournament.control.service.dto.SwissDTO;
import cz.tournament.control.service.util.SwissParticipant;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
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
    public static final double PLAYED_EACH_OTHER_PENALTY = 10000.;
    public static final double ONE_LAST_SAME_PENALTY = 1.;
    public static final double THREE_LAST_SAME_PENALTY = 10000.;
    
    private final Logger log = LoggerFactory.getLogger(SwissService.class);
    
    private final SwissRepository swissRepository;
    private final UserRepository userRepository;
    private final ParticipantService participantService;
    private final GameService gameService;

    public SwissService(SwissRepository swissRepository, UserRepository userRepository, ParticipantService participantService, GameService gameService) {
        this.swissRepository = swissRepository;
        this.userRepository = userRepository;
        this.participantService = participantService;
        this.gameService = gameService;
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
     * Creates a swiss tournament.
     * Generates first round of if swiss has at least 2 participants. 
     * Sets up rounds and roundsToGenerate attributes if at least 2 participants.
     * 
     * @param swissDTO - contains swiss and seeding
     * @return Swiss swiss - created tournament 
     */
    public Swiss createSwiss(SwissDTO swissDTO){
        log.debug("Request to create Swiss : {}", swissDTO.getSwiss());
        Swiss swiss = swissDTO.getSwiss();
        //set user
        User creator = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        swiss.setUser(creator);
        //set creation date
        swiss.setCreated(ZonedDateTime.now());
        
        //generate games if more than 2 participants 
        int n = swiss.getParticipants().size();
        if(n >= 2){
            //set rounds and roundsToGenerate
            int N = n + (n % 2);
            int rounds =  (int) Math.ceil(Math.log(N)/Math.log(2.));
            swiss.rounds(rounds).roundsToGenerate(rounds);
            //save to db
            swiss = swissRepository.save(swiss);
            generateFirstRound(swiss, swissDTO.getSeeding());
        }
        Swiss result = swissRepository.save(swiss);
        
        return result;
    }
    
    /**
     * Updates swiss tournament. Deletes all matches and regenerates first round if needed (round results lost). 
     * 
     * Changes that cause comlpete regeneration of matches: 
     *  1. changed participants -> number of rounds (even just swapping)
     *  2. changed seeding - affects first round (idiot proof on frontend)
     *  ??? 3. changes in points? ??? todo
     *
     * @param swissDTO - contains swiss and seeding
     * @return Swiss - saved swiss system tournament
     */
    public Swiss updateSwiss(SwissDTO swissDTO){
        log.debug("Request to update SwissDTO : {}", swissDTO);
        Swiss swiss = swissDTO.getSwiss();
        
        //get old one from db
        Swiss oldSwiss = findOne(swiss.getId());
        List<Participant> oldSeeding = getSwissSeeding(swiss.getId());
        //detect change and update/regenerate games if necesary
        boolean participantsAreChanged = !Objects.equals(oldSwiss.getParticipants(), swiss.getParticipants());
        boolean seedingIsChanged = !Objects.equals(oldSeeding, swissDTO.getSeeding());
        if(participantsAreChanged || seedingIsChanged){
            //delete games if there are any
            if(!swiss.getMatches().isEmpty()){
                deleteAllMatches(swiss);
            }
            //generate games if more than 2 participants 
            int n = swiss.getParticipants().size();
            if(n >= 2){
                //set rounds and roundsToGenerate
                int N = n + (n % 2);
                int rounds =  (int) Math.ceil(Math.log(N)/Math.log(2.));
                swiss.rounds(rounds).roundsToGenerate(rounds);
                //save to db
                swiss = swissRepository.save(swiss);
                generateFirstRound(swiss, swissDTO.getSeeding());
            }   
            
        }
        return swissRepository.save(swiss);
       
    }
    
    //delete all tournaments matches from tournament and database
    //could be done better, look at it, also in elimination
    private void deleteAllMatches(Swiss swiss){
        List<Game> matches = new ArrayList<>(swiss.getMatches());
        swiss.setMatches(new HashSet<>());
        gameService.delete(matches);
    }
    
    private boolean hasUnfinishedMatch(Swiss swiss){
        for (Game game : swiss.getMatches()) {
            if(!game.isFinished()){
                return true;
            }
        }
        return false;
    }
  
  private PyObject runPythonScript(String tuples) throws RunPythonScriptException{
        try {
            log.debug("running python scrypt");
            Properties props = new Properties(); 
            // Default is 'message' which displays sys-package-mgr bloat 
            // Choose one of error,warning,message,comment,debug 
            props.setProperty("python.verbose", "debug"); 
            
            String javaClassPath = System.getProperty("java.class.path");
            
            props.setProperty("python.path", javaClassPath + "/Lib");
            
            log.debug("java.class.path: {}", javaClassPath);
            log.debug("python.path: {}", props.getProperty("python.path"));
            
            PythonInterpreter.initialize(System.getProperties(), props, new String[] {""});
            
            PythonInterpreter interpreter = new PythonInterpreter();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            long start = System.currentTimeMillis();
            interpreter.execfile(loader.getResourceAsStream("minWeighMaxMatching.py"));
            PyObject pairingStr = interpreter.eval("repr(matching("+ tuples +"))");
            long end = System.currentTimeMillis();
            long elapsedTime = end - start;
            log.debug("elapsedTime = {}", elapsedTime);
            return pairingStr;
        
      } catch (Exception e) {
          //TODO: do print stack trace and perhaps create your own error to throw with proper message
          e.printStackTrace();
          throw new RunPythonScriptException();
      }
  }
  
  private void gotHerelog(int lognumber){
      log.debug("got here: {}", lognumber);
      lognumber++;
  }
    
    /**
     * Generates next round of a tournament if 
     *     a] swiss.roundsToGenerate > 0
     *     b] all matches are finished
     *     c] swiss has at least 2 participants 
     * 
     * @param swiss tournament
     * @return swiss tournament, unchanged if conditions above not met 
     * 
     */
    public Swiss generateNextRound(Swiss swiss) throws RunPythonScriptException{
        log.debug("Request to generate next round - Swiss : {}", swiss);
        swiss = save(swiss);
        int lognumber = 0;
        if(swiss.getRoundsToGenerate() < 0 || hasUnfinishedMatch(swiss) || swiss.getParticipants().size() <= 2){
            log.debug("No next round generated.");
            return swiss;
        }
        //swiss = decrementRoundsToGenerate_andSave(swiss);
        
        int decrementedRoundsToGenerate = swiss.getRoundsToGenerate() -1;
        int round = swiss.getRounds() - decrementedRoundsToGenerate;
        log.debug("round: {}", round);
        gotHerelog(lognumber);
        
        //collect participant game statistics, sort them by id (games attribute probably not needed) TODO
        List<SwissParticipant> swissParticipants = collectParticipantStatistics(swiss);
        gotHerelog(lognumber);
        Collections.sort(swissParticipants, SwissParticipant.IdComparator);
        gotHerelog(lognumber);
        //build a weight matrix (weights are negative for python program)
        int N = swiss.getParticipants().size() + (swiss.getParticipants().size()%2);
        gotHerelog(lognumber);
        Integer[][] matrix = new Integer[N][N]; //[rows][columns]
        for (int row = 0; row < N; row++) {
            for (int col = row + 1; col < N; col++) { 
                Integer evaluation = (-1) * evaluatePair(swissParticipants.get(row), swissParticipants.get(col), swiss);
                matrix[row][col] = evaluation;
            }
        }
        gotHerelog(lognumber);
        //find ideal pairing using that python program
        String tuples = matrixAsTupleList(matrix);
        log.debug("calling runPythonScript(tuples)");
        PyObject pairingStr = runPythonScript(tuples);
        List<Participant> participants = pairingStrToSeeding(pairingStr, swissParticipants);
        
        //generate games
        int numberOfGames = N/2;
        int field = 1;
        for (int i = 0; i < numberOfGames; i++) {
            //possition decided in pairingStrToSeeding()
            Participant pa = participants.get(i);
            Participant pb = participants.get(N-1-i);
            Game game = new Game().tournament(swiss).round(round).rivalA(pa).rivalB(pb);
            
            //set finished if one is bye
            if(pa.isBye()||pb.isBye()){
                game.setFinished(Boolean.TRUE);
            }
            //set fields, don't set field if it's a dummy bye game
            if(swiss.getPlayingFields() != null && swiss.getPlayingFields() > 1 && !pa.isBye() && !pb.isBye()){
                game.setPlayingField(field);
                field += 1;
                if(field > swiss.getPlayingFields()){
                    field = 1;
                }
            }
            Game savedGame = gameService.createGame(game);
            swiss.addMatches(savedGame);
        }
        
        swiss.setRoundsToGenerate(decrementedRoundsToGenerate);
        
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
        return swissRepository.findByUserIsCurrentUserAndInCombinedFalse(pageable);
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
    public List<Participant> getSwissSeeding(Long id) {
        if(id == null){ return null; }
        Swiss swiss = findOne(id);
        if(swiss == null){return null;}
        
        //get matches and sort them by round
        List<Game> matches = new ArrayList<>(swiss.getMatches());
        if(matches.isEmpty()) return null;
        Collections.sort(matches, Game.RoundComparator);
        
        int N = swiss.getParticipants().size() + swiss.getParticipants().size()%2;
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
     * Generates first round of a swiss tournament.
     * Expects there to be more than 1 participant.
     * Decrements roundsToGenerate.
     * Sets field for non-BYE matches.
     * 
     * @param swiss tournament with proper amount of participants
     * @param seeding from frontend, bye included
     */
    private void generateFirstRound(Swiss swiss, List<Participant> seeding) {
        //prepare roundCounts 
        int n = swiss.getParticipants().size();
        int N = n + (n % 2);
        int numberOfGames = N/2;
        
        //generate games with or without seeding 
        List<Participant> participants = prepareParticipants(swiss.getParticipants(), seeding, N, n);
        
        swiss = decrementRoundsToGenerate_andSave(swiss);
        
        int field = 1;
        for (int i = 0; i < numberOfGames; i++) {
            Participant pa = participants.get(i);
            Participant pb = participants.get(N-1-i);
            Game game = new Game().tournament(swiss).round(1).rivalA(pa).rivalB(pb);
            //set finished if one is bye
            if(pa.isBye()||pb.isBye()){
                game.setFinished(Boolean.TRUE);
            }
            //set fields, don't set fields if it's a dummy bye game
            if(swiss.getPlayingFields() != null && swiss.getPlayingFields() > 1 && !pa.isBye() && !pb.isBye()){
                game.setPlayingField(field);
                field += 1;
                if(field > swiss.getPlayingFields()){
                    field = 1;
                }
            }
            Game savedGame = gameService.createGame(game);
            swiss.addMatches(savedGame);
        }
    }

    /**
     * Prepares participants to create games from.
     *  1. chooses between tournaments participants or seeding 
     *  2. checks for valid seeding length (IllegalArgumentE.)
     *  3. checks for valid number of participants (n = N or N-1) (IllegalStateE.)
     *  4. adds Bye to participants if needed (seeding has it from frontend)
     * 
     * @param participants
     * @param seeding
     * @param N
     * @param n
     * @return 
     */
    private List<Participant> prepareParticipants(Set<Participant> participants, List<Participant> seeding, int N, int n) {
        if(seeding != null && !seeding.isEmpty()){
            if(seeding.size() < 2) throw new IllegalArgumentException("Swiss tournaments seeding has wrong length = " +seeding.size());
            return seeding;
        }
        if(n != N && n != N-1) throw new IllegalStateException("Swiss tournament has wrong number of participants.");
        
        ArrayList<Participant> result = new ArrayList<>(participants);
        if(n!=N){
            Participant BYE = participantService.getByeParticipant();
            result.add(BYE);
        }
        return result;
    }

    private Swiss decrementRoundsToGenerate_andSave(Swiss tmp) {
        int rtg = tmp.getRoundsToGenerate() - 1;
        tmp.roundsToGenerate(rtg);
        return swissRepository.save(tmp);
    }
    
    /**
     * Because innate List.indexOf() didn't work.
     * @param participant
     * @param swissParticipants
     * @return int >= 0 if found there, -1 if not 
     */
    private int getIndexOf(Participant participant, List<SwissParticipant> swissParticipants){
        for (int i = 0; i < swissParticipants.size(); i++) {
//            if(Objects.equals(participant, swissParticipants.get(i).getParticipant())){
//                return i;
//            }
              if(Objects.equals(participant.getId(), swissParticipants.get(i).getParticipant().getId())){
                    return i;
                }
        }
        return -1;
    }

    /**
     * Assumes that all games of swiss tournament are finished.
     * 
     * @param swiss
     * @return 
     */
    private List<SwissParticipant> collectParticipantStatistics(Swiss swiss) {
        log.debug("collecting Participant Statistics");
        List<SwissParticipant> swissParticipants = new ArrayList<>();
        for (Participant participant : swiss.getParticipants()) {
            swissParticipants.add(new SwissParticipant().participant(participant));
        }
        if(swiss.getParticipants().size()%2==1){ // add bye if needed, byes are not saved in tournaments participants
            swissParticipants.add(new SwissParticipant().participant(participantService.getByeParticipant()));
        }
        List<Game> matches = new ArrayList<>(swiss.getMatches());
        Collections.sort(matches, Game.RoundComparator); //needed sorted by round cause of colorStr
        
        for (Game game : matches) {
            int indexA = getIndexOf(game.getRivalA(), swissParticipants);
            int indexB = getIndexOf(game.getRivalB(), swissParticipants);
            SwissParticipant spA = swissParticipants.get(indexA);
            SwissParticipant spB = swissParticipants.get(indexB);
            
            //add game to those swissP. might not be needed 
            spA.addGame(game);
            spB.addGame(game);
            
            //add rivals
            spA.addRival(game.getRivalB());
            spB.addRival(game.getRivalA());
            
            //add position/color string
            spA.appendColorString("a");
            spB.appendColorString("b");
            
            //adding points
            Map<String, Participant> winnerAndLoser = game.getWinnerAndLoser();
            //if tie
            if(winnerAndLoser.get("winner") == null){
                spA.incrPoints(swiss.getPointsForTie());
                spB.incrPoints(swiss.getPointsForTie());
            }
            //A winner
            if( Objects.equals(winnerAndLoser.get("winner"),game.getRivalA()) ){
                spA.incrPoints(swiss.getPointsForWinning());
                spB.incrPoints( (-1) * swiss.getPointsForLosing() );
            }
            //B winner
            if( Objects.equals(winnerAndLoser.get("winner"),game.getRivalB()) ){
                spB.incrPoints(swiss.getPointsForWinning());
                spA.incrPoints( (-1) * swiss.getPointsForLosing() );
            }
            //just to be sure
            swissParticipants.set(indexA, spA);
            swissParticipants.set(indexB, spB);
        }
        return swissParticipants;
    }
    
    /**
     * Copute weight of pairs matchup.
     * + adds points * 10 
     * + ridiculously high penalty if they have already played each other
     * + penalty for color if swiss.color = true
     * 
     * Double result rounded up and converted to Integer because puthon 
     * matching program computes maxCardinality only for integers, 
     *      - shouldn't be a problem if points have at most one decimal place
     * 
     * @param rowp - rival
     * @param colp - the other rival
     * @param swiss - tournament 
     * @return double, rounded up, converted to int
     */
    private Integer evaluatePair(SwissParticipant rowp, SwissParticipant colp, Swiss swiss) {
        Double eval = 0.;
        //points
        eval += Math.abs(rowp.getPoints() - colp.getPoints())*10;
        
        //check if played each other before
        if(rowp.getRivals().contains(colp.getParticipant()) || colp.getRivals().contains(rowp.getParticipant())){
            eval += PLAYED_EACH_OTHER_PENALTY;
        }
        //last leters same 
        if(swiss.isColor()){
            eval += Double.max(determineColorStringPenalty_oneWay(rowp.getColorStr(), colp.getColorStr()), 
                               determineColorStringPenalty_oneWay(colp.getColorStr(), rowp.getColorStr()) );
        }
        Double result = Math.ceil(eval);
        return result.intValue();
       
    }
    
    /**
     * Computes penalty according to last three letters of colorString.
     * Should be done twice with switched participants.
     * 
     *  Penalty matrix:
     * ==================
     *       A   AA   AAA
     *   A   1    5    50
     *  AA   5   10   500
     * AAA  50  500  1000
     * 
     * @param str1
     * @param str2
     * @return 
     */
    private Double determineColorStringPenalty_oneWay(String str1, String str2){
        Double penalty = 0.;
        int len = str1.length(); //both should always have the same length
        char[] arr1 = str1.toCharArray();
        char[] arr2 = str2.toCharArray();
        int lastSameNumber = getLastSameNumber(arr1);
        
        if(lastSameNumber >= 3 && arr1[len-1] == arr2[len-1]){//AAA-A
            penalty += 50;
            if(arr1[len-1] == arr2[len-2]){//AAA-AA
                penalty += 450;
                if(arr1[len-1] == arr2[len-3]){//AAA-AAA
                penalty += 500;
                }
            }
        }
        if(lastSameNumber == 2 && arr1[len-1] == arr2[len-1]){//AA-A
            penalty += 5;
            if(arr1[len-1] == arr2[len-2]){//AA-AA
                penalty += 5;
            }
        }
        if(lastSameNumber == 1 && arr1[len-1] == arr2[len-1]){//A-A
            penalty += 1;
        }
        return penalty;
    }
    
    /**
     * Returns lenght of uninterrupted sequence of same letters from the end of arr.
     * example:    ''= 0;   'a'= 1;  'aa'= 2;
     *          'aaa'= 3; 'baa'= 2; 'aba'= 1
     *
     * @param arr - colorStr converted to char array
     * @return int number 
     */
    private int getLastSameNumber(char[] arr) {
        int len = arr.length;
        int res = 0;
        if(len == 0) return res;
        res+=1;
        for (int i = len-1; i > 0; i--) {
            if(arr[i]==arr[i-1]){
                res +=1;
            }else{
                break;
            }
            
        }
        return res;
    } 
    

    /**
     * Integer[][] -> "[ (row, col, value), (row, col, value),(row, col, value),...]"
     * 
     * + does not make tuples where row == col - those don't even need to be considered
     * + does not make mirroring tuples - puthon matching program seems to not need them
     *  
     * @param matrix
     * @return 
     */
    private String matrixAsTupleList(Integer[][] matrix) {
        String tuples = "[";
        int N = matrix.length;
        for (int row = 0; row < N; row++) {
            for (int col = row + 1; col < N; col++) {
                String tuple = "("+row+","+col+","+matrix[row][col]+")";
                tuples = tuples.concat(tuple);
                if(row == N-2 && col == N-1){
                    tuples = tuples.concat("]");
                    break;
                }else{
                    tuples = tuples.concat(",");
                }
            }
        }
        return tuples;
    }

    /**
     * "[int,int,int,...]" -> seeding like list of participants
     * + also decides possition of each rival
     * 
     * seeding:
     *  [A,B,C,D,E,F]
     *   | | | | | |
     *   | | +-+ | |
     *   | +-----+ |
     *   +---------+
     * 
     * @param pairingStr - given by python program, [int,int,int...]
     * @param swissParticipants - sorted by their id attribute 
     * @return List<Participant> - seeding like, see above
     */
    private List<Participant> pairingStrToSeeding(PyObject pairingStr, List<SwissParticipant> swissParticipants) {
        List<Integer> pairing = convertPyObjectToIntArray(pairingStr);
        if(pairing.contains(-1))throw new IllegalArgumentException("Pairing was not complete.");
        if(pairing.size()%2==1)throw new IllegalArgumentException("Odd length of pairing. This should never happen.");
                
        int N = swissParticipants.size();
        Participant[] seeding = new Participant[N];
        Set<Integer> processedIndexes = new HashSet<>();
        int lastSeedingIndex = 0;
        
        for (int i = 0; i < N; i++) {
            if(processedIndexes.size() == N) break; //all indexes processed
            int indexOfB = pairing.get(i);
            if(!processedIndexes.contains(i) && !processedIndexes.contains(indexOfB)){
                //decide A/B position (lesser index is A)
                Participant[] position = decidePosition(swissParticipants.get(i), swissParticipants.get(indexOfB));
                
                seeding[lastSeedingIndex] = position[0];
                seeding[N-1-lastSeedingIndex]=position[1];
                
                processedIndexes.add(i);
                processedIndexes.add(indexOfB);
                
                lastSeedingIndex += 1;
            }
        }
        return new ArrayList<>(Arrays.asList(seeding));
        
    }
    
    /**
     * Decides which rival goes to A and which to B.
     * 
     * @param sp1
     * @param sp2
     * @return Participant[] - [A, B]
     *                          0  1
     */
    private Participant[] decidePosition(SwissParticipant sp1, SwissParticipant sp2) {
        //bigger number goes to B - result[1]
        int score1 = getLetterScore(sp1.getColorStr().toCharArray());
        int score2 = getLetterScore(sp2.getColorStr().toCharArray());
        if(score1 >= score2){
            return new Participant[]{sp2.getParticipant(), sp1.getParticipant()};
        }
        return new Participant[]{sp1.getParticipant(), sp2.getParticipant()};
        
    }
    
    /**
     * Computes participants score for place B based on last 3 letters of colorString. 
     * Bigger score goes to B.
     * 
     * if A _ _ add   1
     * if _ A _ add  10
     * if _ _ A add 100
     * 
     * @param colorStr of a participant whose score for place B we want to compute
     * @return int score in range 0 -> 111
     */
    private static int getLetterScore(char[] colorStr) {
        int result = 0;
        int len = colorStr.length;
        if(len == 0) return 0;
        int multiplicant = 100;
        for (int i = len-1; i >= Integer.max(0, len-3); i--) {
            if(colorStr[i] == 'a'){
                result += multiplicant;
            }
            multiplicant = multiplicant/10;
        }
        return result;
    }
    
    /**
     * PyObject: "[1,0,3,2]" -> List<Integer> [1,0,3,2]
     * @param pairingStr
     * @return 
     */
    private List<Integer> convertPyObjectToIntArray(PyObject pairingStr) {
        String str = pairingStr.toString();
        String[] parts = str.split("[\\[ \\] \\,]");
        List<Integer> pairing = new ArrayList<>();
        
        for (String part : parts) {
            if (!part.trim().equals("")) {
                pairing.add(Integer.valueOf(part.trim()));
            }
        }
        return pairing;
    }

//    /*
//    to test tour getLetterScore()
//    */
//    public static void main(String[] args) {
//        System.out.println("'', "+getLetterScore("".toCharArray()));
//        System.out.println("'a', "+getLetterScore("a".toCharArray()));
//        System.out.println("'b', "+getLetterScore("b".toCharArray()));
//        
//        System.out.println("'aa', "+getLetterScore("aa".toCharArray()));
//        System.out.println("'ba', "+getLetterScore("ba".toCharArray()));
//        System.out.println("'ab', "+getLetterScore("ab".toCharArray()));
//        System.out.println("'bb', "+getLetterScore("bb".toCharArray()));
//        
//        System.out.println("'aaa', "+getLetterScore("aaa".toCharArray()));
//        System.out.println("'aab', "+getLetterScore("aab".toCharArray()));
//        System.out.println("'aba', "+getLetterScore("aba".toCharArray()));
//        System.out.println("'baa', "+getLetterScore("baa".toCharArray()));
//        System.out.println("'bbb', "+getLetterScore("bbb".toCharArray()));
//        System.out.println("'bba', "+getLetterScore("bba".toCharArray()));
//        System.out.println("'bab', "+getLetterScore("bab".toCharArray()));
//        System.out.println("'abb', "+getLetterScore("abb".toCharArray()));
//        
//    }
    

    
}
