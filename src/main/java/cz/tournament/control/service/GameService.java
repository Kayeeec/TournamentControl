/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import com.google.common.base.Objects;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.GameSet;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.enumeration.EliminationType;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.EliminationRepository;
import cz.tournament.control.repository.GameRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class GameService {
    private final Logger log = LoggerFactory.getLogger(GameService.class);
    private final GameRepository gameRepository;
    private final GameSetService gameSetService;
    private final SetSettingsService setSettingsService;
    private final TournamentService tournamentService;
    private final EliminationRepository eliminationRepository;

    public GameService(GameRepository gameRepository, GameSetService gameSetService, SetSettingsService setSettingsService, TournamentService tournamentService, EliminationRepository eliminationRepository) {
        this.gameRepository = gameRepository;
        this.gameSetService = gameSetService;
        this.setSettingsService = setSettingsService;
        this.tournamentService = tournamentService;
        this.eliminationRepository = eliminationRepository;
    }
    
    /**
     * 
     * @param game
     * @return 
     */
    public Game save(Game game) {
        log.debug("Request to save Game : {}", game);
        Game result = gameRepository.save(game);
        return result;
    }
    
    private boolean isBye(Participant rival){
        if(rival == null) return false;
        return rival.isBye();
    }

    
    public Game createGame(Game game){
        log.debug("Request to create Game : {}", game);
        Game tmp = gameRepository.save(game); 
        
        SetSettings defaultSetSettings = setSettingsService.save(game.getTournament().getSetSettings()) ;
        
        if((game.getRivalA()==null || !game.getRivalA().isBye()) && (game.getRivalB()==null || !game.getRivalB().isBye())){
            //prepare sets - one or number of sets to win
            GameSet set = gameSetService.save(new GameSet().game(tmp).setSettings(defaultSetSettings));
            tmp.addSets(set);
            Integer sets = tmp.getTournament().getSetsToWin();
            if (sets != null && sets > 1) {
                for (int i = 1; i < sets; i++) {
                    set = gameSetService.save(new GameSet().game(tmp).setSettings(defaultSetSettings));
                    tmp.addSets(set);
                }
            }
        }
        
        Game result = gameRepository.save(tmp);
        return result;
    }
    
    private String getSetsString(Set<GameSet> sets){
        String result = "";
        for (GameSet set : sets) {
            result = result + set.getId() + ", ";
        }
        return result;
    }
    
    public Game updateGame(Game game){
        String debug = "Request to update Game : " + game;
        Game oldGame = gameRepository.findOne(game.getId());
        //ensure tournament - never changes
        if(game.getTournament() == null){
           Tournament oldTournament = oldGame.getTournament();
           game.setTournament(oldTournament); 
        }
        
//        log.debug("old sets: {}", getSetsString(oldGame.getSets()));
//        log.debug("new game: {}", getSetsString(game.getSets()));
//        
        //trigger removal
        Set<Long> newIds = new HashSet<>();
        for (GameSet set : game.getSets()) {
            newIds.add(set.getId());
        }
        for (GameSet oldSet : oldGame.getSets()) {
            if(!newIds.contains(oldSet.getId())){
                gameSetService.delete(oldSet.getId());
            }
        }
        
        //trigger create and update sets, ensure game
        for (GameSet set : game.getSets()) {
            set.game(game);
            gameSetService.updateGameSet(set);
        }
        
        //finish the game if all sets are finished and is not an unallowed tie 
        game.setFinished(Boolean.FALSE);
        if(game.getRivalA() != null && game.getRivalB() != null){
            if(game.getRivalA().isBye() || game.getRivalB().isBye()){
                game.setFinished(Boolean.TRUE);
            }
            if(game.getTournament().getTiesAllowed()){
                if(game.allSetsFinished()){
                    game.setFinished(Boolean.TRUE);
                } 
            }else{
               if(game.allSetsFinished_And_NotATie()){
                   game.setFinished(Boolean.TRUE);   
               } 
            }
        }
        
        //if tournament is elimination update next games
        Tournament tournament = tournamentService.findOne(game.getTournament().getId());
        if(tournament instanceof Elimination){
            log.debug("Tournament is instance of Elimination, calling nextGameUpdate().");
            elimination_nextGameUpdate(game, tournament);
        }
        
        Game result = gameRepository.save(game);
        log.debug(debug + ", with result: {}", result);
        return result;
    }
    
    /**
     * Creates a new set for game with tournaments setSettings.
     * @param game
     * @return
     */
    public Game addNewSet(Game game){
        SetSettings setSettings = gameRepository.findOne(game.getId()).getTournament().getSetSettings();
        GameSet set = gameSetService.save(new GameSet().game(game).setSettings(setSettings));
        log.debug("GAME_SERVICE..........added set: {}, game= {}",set.getId(), game.toString());
        game.addSets(set);
        Game result = gameRepository.save(game);
        log.debug("GAME_SERVICE: saved game: {} with sets {}", game.toString(), game.setsToString());
        return result;
    }
    
    /**
     *  Get one game by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Game findOne(Long id) {
        log.debug("Request to get Game : {}", id);
        Game game = gameRepository.findOne(id);
        return game;
    }
    
    /**
     *  Get all the games.
     *  
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Game> findAll() {
        log.debug("Request to get all Games");
        List<Game> result = gameRepository.findAll();
        return result;
    }
    
    /**
     *  Get all the games that have a certain tournament.
     *  
     * @param tournament of games returned
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Game> findGamesByTournament(Tournament tournament) {
        log.debug("Request to get all Games with tournament: {}", tournament);
        List<Game> result = gameRepository.findByTournament(tournament);
        return result;
    }
    
     /**
     *  Delete the game by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Game : {}", id);
        Game game = gameRepository.findOne(id);
        gameSetService.delete(new ArrayList<>(game.getSets()));
        gameRepository.delete(id);
    }
    
    /**
     *  Delete list of games.
     *
     * 
     * @param games list of entities to delete 
     */
    public void delete(List<Game> games) {
        log.debug("Request to delete list of games : {}", games.toString());
        for (Game game : games) {
            gameSetService.delete(new ArrayList<>(game.getSets()));
        }
        gameRepository.delete(games);
    }
    
    /*****************************/
    /*     update game logic     */
    /*****************************/
    
    private int getRootIndex(Elimination tournament) {
        int N = tournament.getN();
        if (tournament.getType().equals(EliminationType.SINGLE)) {
            return (N - 2);
        } else { //(tournament.getType().equals(EliminationType.DOUBLE))
            return ((2 * N) - 3);
        }
    }
    
    private int getParentIndex(int index, int round, int N, Elimination tournament){
        int rootIndex = getRootIndex(tournament);
        
        if(tournament.getType().equals(EliminationType.SINGLE)){
            if(index == N-2) return -1;
            return (int) Math.floor((index + rootIndex)/2) + 1;
        }
        else{//DOUBLE
            int winnerRoot = (N - 2);
            int loserRoot = ((2*N)-4);
            
            if(index == winnerRoot || index == loserRoot ){
                return rootIndex;
            }
            if(index < winnerRoot){
                return (int) Math.floor((index + winnerRoot)/2)+1;
            }
            if(index < loserRoot){
                int segmentSize = N/((int)Math.pow(2, Math.ceil(((double)(-1)*round)/10)));
                if(round % 10 != 0){ //15, 25, 35...
                    return (index + segmentSize);   
                }
                //else 20,30,40...
                int inSegmentIndex;
                if(index % 2 == 1){ 
                    inSegmentIndex = ((index + 1) % segmentSize)/2;
                    return index + segmentSize - inSegmentIndex;
                }else{
                    inSegmentIndex = ((index) % segmentSize)/2;
                    return (index-1) + segmentSize - inSegmentIndex;
                }
            }
            if(index == rootIndex){
                if(tournament.getBronzeMatch()){
                    return 2*N-1;
                }
                return 2*N-2;
            }
            /* if index >= rootindex 
                    index == rootindex solved before, 
                    index > rootindex - has no next match */
            return -1;
        }
    }

    
    private int getLoserRound(int round){
        if(round == 1){
            return -15;
        }
        return round*10*(-1);
    }
    
    private List<Game> getLoserGamesForGame(Game game, List<Game> matches, int N){
        List<Game> loserGames = new ArrayList<>();
        int loserRound = getLoserRound(game.getRound());
        
        for (int i = N-1; i <= 2*N-4; i++) {
            Game loserGame = findOne(matches.get(i).getId());
            if(loserGame.getRound()<loserRound){
                break;
            }
            if(loserGame.getRound()==loserRound){
                loserGames.add(loserGame);
            }       
        }
        return loserGames;
    }

    
    /**
     * Returns index of a next final match in double elimination tournament with regards to possibility of bronze match.
     * @param N     next power of two bigger or equal to number of participants
     * @param matchesSize   number of created matches (length of matches array)
     * @param elimination   double elimination tournament
     * @return -1 if next final match does not exist, number bigger than it otherwise
     */
    private int nextFinalIndex(int N, int matchesSize, Elimination elimination){
        int nextFinalIndex = 2*N-2;
        if(elimination.getBronzeMatch()){
            nextFinalIndex += 1;
        }
        if(matchesSize-1 == nextFinalIndex){
            return nextFinalIndex;
        }
        return -1;
    }
    
    
    /**
     * Number of winner tree (or single elimination tournament) rounds
     * @param N next power of two bigger or equal to number of participants
     * @return Number of winner tree (or sindle elimination tournament) rounds.
     */
    private int numberOfRounds(int N) {
        return (int) (Math.log(N) / Math.log(2));
    }
    
    /**
     * Sets winner into given game according to rules.
     * Can be used for single elimination but make sure comesFrom is not root. (Use up to root)
     * Does not solve absolute root - newFinalGame
     * RULES: comes from...
     * 1] winner root: winner to A
     * 2] loser root:  winner to B
     * 3] round is 1..number_of_rounds OR 20, 30, 40 ...
     *      index = even: winner to A
     *      index = odd:  winner to B
     * 4] round is 15, 25, 35 ...
     *      winner to A
     * @param progressInto  next game to progress into  
     * @param winner    winner of game (comesFrom)
     * @param comesFrom game from which the winner came
     * @param N     next power of two bigger or equal to number of participants
     * @return modified next game, with winner in proper place
     */
    private Game progressWinner_returnGame(Game progressInto, Participant winner, Game comesFrom, int N){
        int round = comesFrom.getRound();
        int index = comesFrom.getPeriod() - 1;
        if(index == N-2){//winner root
            progressInto.setRivalA(winner);
            return progressInto;
        }
        if(index == 2*N-4){//loser root
            progressInto.setRivalB(winner);
            return progressInto;
        }
        if( (0 < round && round < numberOfRounds(N)) || (round < 0 && round % 10 == 0)){ //from winner or round is -20, -30, -40 ...
            if(index % 2 == 0){
                progressInto.setRivalA(winner);
                return progressInto;
            }
            progressInto.setRivalB(winner);
            return progressInto;
        }
        //round -15,-25,-35...
        progressInto.setRivalA(winner);
        return progressInto;
        
    }
    
    /**
     * Prefers A.
     * @param properRoundLoserMatches
     * @param loser
     * @return 
     */
    private Game seedAByeLoserIntoFirstLoserRound(List<Game> properRoundLoserMatches, Participant loser){
        Game firstEmptyB = null;
        for (Game possibleLoserGame : properRoundLoserMatches) {
            if(possibleLoserGame.getRivalA() == null){
                possibleLoserGame.setRivalA(loser);
                return possibleLoserGame;
            }
            if(firstEmptyB == null && possibleLoserGame.getRivalB() == null){
                firstEmptyB = possibleLoserGame;
            }
        }//no empty A
        if(firstEmptyB == null) throw new IllegalStateException("Seeding into loser bracket round 1: no empty space for loser found.");
        firstEmptyB.setRivalB(loser);
        return firstEmptyB;
    }
    
    /**
     * Pics a looser bracket or bronze game to put loser into according to rules. 
     * Only for double elimination tournament.
     * RULES: 
     * 1] tournament has bronze match 
     *      comesFrom.index == 2*N-5 : loser to bronze match : A
     *      comesFrom.index == 2*N-4 : loser to bronze match : B
     * 2] comes from winner tree (index up to N-2)
     *      round = 1: seeds into either place previously occupied by comesFrom rival OR first available place 
     *      round > 1: puts loser into either B previously occupied by comesFrom rival OR first empty B place 
     * 
     * @param matches all tournament matches, loser matches extracted inside
     * @param loser loser of the comesFrom game 
     * @param comesFrom the game from wich loser came 
     * @param N next power of two bigger or equal to number of participants
     * @param elimination this tournament
     * @return modified loserGame, with loser put in proper place, or null - indicates weird state
     */
    private Game progressLoser_returnGame(List<Game> matches, Participant loser, Game comesFrom, int N, Elimination elimination){
        int index = comesFrom.getPeriod() - 1;
        //bronze
        if(elimination.getBronzeMatch()){
            Game bronze = findOne(matches.get(matches.size()-1).getId());
            if(index == 2*N-5){
                bronze.setRivalA(loser);
                return bronze;
            }
            if(index == 2*N-4){
                bronze.setRivalB(loser);
                return bronze;
            }
        }
        int winnerIndex = N-2;
        if(index <= winnerIndex){
            int round = comesFrom.getRound();
            List<Game> possibleLoserGames = getLoserGamesForGame(comesFrom, matches, N);
            if(round == 1){
                //here seeding into loser bracket happens
                if(loser != null && loser.isBye()){//these only into null and prefer A
                    return seedAByeLoserIntoFirstLoserRound(possibleLoserGames, loser);
                }else{
                    Game firstEmpty = null;
                    for (Game loserMatch : possibleLoserGames) {
                        if(firstEmpty == null && (loserMatch.getRivalA() == null || loserMatch.getRivalB() == null) ){
                            firstEmpty = loserMatch;
                        }
                        if(Objects.equal(loserMatch.getRivalA(), comesFrom.getRivalA())
                                || Objects.equal(loserMatch.getRivalA(), comesFrom.getRivalB()) ){
                            loserMatch.setRivalA(loser);
                            return loserMatch;
                        }
                        if(Objects.equal(loserMatch.getRivalB(), comesFrom.getRivalA())
                                || Objects.equal(loserMatch.getRivalB(), comesFrom.getRivalB()) ){
                            loserMatch.setRivalB(loser);
                            return loserMatch;
                        }
                    }
                    if(firstEmpty == null) throw new IllegalStateException("Seeding into loser bracket round 1: no empty space for loser found.");
                    if(firstEmpty.getRivalA() == null){
                        firstEmpty.setRivalA(loser);
                        return firstEmpty;
                    }
                    firstEmpty.setRivalB(loser);
                    return firstEmpty;
                }
            }else{
                //in case of modification first look for itself or winner but remember a first null 
                Game firstEmpty = null;
                for (Game loserMatch : possibleLoserGames) {
                    if(firstEmpty == null && loserMatch.getRivalB() == null){
                        firstEmpty = loserMatch;
                    }
                    //only on B
                    if(Objects.equal(loserMatch.getRivalB(), comesFrom.getRivalA())
                            || Objects.equal(loserMatch.getRivalB(), comesFrom.getRivalB()) ){
                        loserMatch.setRivalB(loser);
                        return loserMatch;
                    }
                    //comparison problems
                    if(loserMatch.getRivalB() == comesFrom.getRivalA() 
                            || loserMatch.getRivalB() == comesFrom.getRivalB() ){
                        loserMatch.setRivalB(loser);
                        return loserMatch;
                    }
                    log.debug("***");
                    log.debug("loser on B: {}", loserMatch.getRivalB());
                    log.debug("comesFrom on A: {}", comesFrom.getRivalA());
                    log.debug("comesFrom on B: {}", comesFrom.getRivalB());
                }
                //not a modification => put on first available null
                log.debug("****firstEmpty: {}, loser: {}", firstEmpty, loser);
                firstEmpty.setRivalB(loser);
                return firstEmpty;
            }
        }
        return null; //nothing happened
    }
    
    private void singleElimination_nextGameUpdate(Game game, Elimination elimination) {
        List<Game> matches = new ArrayList<>(elimination.getMatches());
        Collections.sort(matches, Game.PeriodRoundComparator);
        int N = elimination.getN();
        int index = game.getPeriod() - 1;
        int rootIndex = N-2, bronzeIndex = N-1;
        if(index == rootIndex || index == bronzeIndex) return;
        
        Map<String, Participant> winnerAndLoser = game.getWinnerAndLoser();
        int winnerGameIndex = getParentIndex(index, game.getRound(), N, elimination);
        log.debug("*** single> index: {}, winner = {}, loser = {}, winnerGameIndex = {}", 
                index, winnerAndLoser.get("winner"), winnerAndLoser.get("loser"), winnerGameIndex);
        if(winnerGameIndex != -1){
            Game nextGame_modified = progressWinner_returnGame(matches.get(winnerGameIndex), 
                    winnerAndLoser.get("winner"), game, N);
            log.debug("***** nextGame_modified> {}", nextGame_modified);
            Game updatedWinnerGame = updateGame(nextGame_modified);
            log.debug("***** updatedWinnerGame> {}", updatedWinnerGame);
        }
        //bronze
        if(elimination.getBronzeMatch()){
            if(index == N-4){
                Game bronze = findOne(matches.get(bronzeIndex).getId());
                bronze.setRivalA(winnerAndLoser.get("loser"));
                updateGame(bronze);
            }
            if(index == N-3){
                Game bronze = findOne(matches.get(bronzeIndex).getId());
                bronze.setRivalB(winnerAndLoser.get("loser"));
                updateGame(bronze);
            }
        } 
    }
    
    private void doubleElimination_nextGameUpdate(Game game, Elimination elimination){
        List<Game> matches = new ArrayList<>(elimination.getMatches());
        Collections.sort(matches, Game.PeriodRoundComparator);
        int N = elimination.getN();
        int index = matches.indexOf(game);
        int newFinalIndex = nextFinalIndex(N, matches.size(), elimination);
        if(newFinalIndex == index) return ;
        int root = 2*N - 3;
        Map<String, Participant> winnerAndLoser = game.getWinnerAndLoser();
        
        if(index == root){
            //special case if game is final AND the one who lost once won
            Game previousLoserGame = matches.get(index-1);
            
            if(Objects.equal(winnerAndLoser.get("winner"), previousLoserGame.getRivalA())
                    || Objects.equal(winnerAndLoser.get("winner"), previousLoserGame.getRivalB()) ){
                int period = game.getPeriod()+1;
                if(elimination.getBronzeMatch()){
                    period += 1;
                }
                log.debug("**** creating newFinal. trigerring game index {} root {}", index, root);
                Game nextFinal = new Game().tournament(elimination).period(period).round(game.getRound()+1)
                        .rivalA(game.getRivalB()).rivalB(game.getRivalA());
                Game savedNextFinal = createGame(nextFinal);
                elimination.addMatches(savedNextFinal);
                eliminationRepository.save(elimination);
                return;
            }
            //game got changed, rival from winner bracket won => delete next final if it exists
            if(newFinalIndex != -1){
                Game nextFinal = matches.get(newFinalIndex);
                elimination.removeMatches(nextFinal);
                eliminationRepository.save(elimination);
                this.delete(nextFinal.getId());
                return; 
            }  
        }if(index < root){
            int winnerGameIndex = getParentIndex(index, game.getRound(), N, elimination);
            Game winnerGame = findOne(matches.get(winnerGameIndex).getId());
            log.debug("*** game before progress winner: id: {}, A: {}, B: {}", game.getId(), game.getRivalA(), game.getRivalB());
            if(winnerGameIndex != -1){
                Game winnerGame_modified = progressWinner_returnGame(winnerGame, winnerAndLoser.get("winner"), game, N);
                this.updateGame(winnerGame_modified);
            }
            log.debug("*** game after progress winner: id: {}, A: {}, B: {}", game.getId(), game.getRivalA(), game.getRivalB());
            Game loserGame_modified = progressLoser_returnGame(matches, winnerAndLoser.get("loser"), game, N, elimination);
            if(loserGame_modified != null){
                this.updateGame(loserGame_modified);
            }else {
                log.debug("***doubleElimination_nextGameUpdate: loserGame_modified was null: round {} index {} rivalA {} rivalB {} loser {}",
                        game.getRound(), index, game.getRivalA(), game.getRivalB(), winnerAndLoser.get("loser"));
            }
        } 
    }
    
    private void elimination_nextGameUpdate(Game game, Tournament tournament) {
        Elimination elimination = (Elimination) tournament;
        if (game.isFinished()) {
            if (elimination.getType().equals(EliminationType.SINGLE)) {
                singleElimination_nextGameUpdate(game, elimination);
            } else {
                doubleElimination_nextGameUpdate(game, elimination);
            }
        }

        //game got unfinished
        Game old = this.findOne(game.getId());
        if (old.isFinished() && !game.isFinished()) {
            List<Game> matches = new ArrayList<>(elimination.getMatches());
            Collections.sort(matches, Game.PeriodRoundComparator);
            
            propagateRivalsRemoval(game, matches, elimination);
        }
    }
    
    private void propagateRivalsRemoval(Game game, List<Game> matches, Elimination elimination){
        int index = matches.indexOf(game);
        
        List<Game> gamesToRemoveFrom = new ArrayList<>();
        for (int i = index + 1; i < matches.size(); i++) {
            Game match = matches.get(i);
            if(Objects.equal(match.getRivalA(), game.getRivalA()) 
                    || Objects.equal(match.getRivalB(), game.getRivalA())
                    || Objects.equal(match.getRivalA(), game.getRivalB())
                    || Objects.equal(match.getRivalB(), game.getRivalB())
                    ){
                if( !isBye(match.getRivalA()) || !isBye(match.getRivalB()) ){
                    gamesToRemoveFrom.add(match);
                }
            }
        }
        log.debug("rivals removal: index= {}, game= {}, games= {}", index,game, gamesToRemoveFrom);
        if(gamesToRemoveFrom.isEmpty()) return;
                
        for(int i = gamesToRemoveFrom.size() - 1; i >= 0; i--){
            Game toRemoveFrom = gamesToRemoveFrom.get(i);
            propagateRivalsRemoval(toRemoveFrom, matches, elimination);
            if(!isBye(toRemoveFrom.getRivalA()) &&
                    ( Objects.equal(toRemoveFrom.getRivalA(), game.getRivalA())
                    || Objects.equal(toRemoveFrom.getRivalA(), game.getRivalB()) ) 
                    ){
                toRemoveFrom.setRivalA(null);
            }
            if(!isBye(toRemoveFrom.getRivalB()) && 
                    ( Objects.equal(toRemoveFrom.getRivalB(), game.getRivalA())
                    || Objects.equal(toRemoveFrom.getRivalB(), game.getRivalB()) )
                    ){
                toRemoveFrom.setRivalB(null);
            }
            if(!isBye(toRemoveFrom.getRivalA()) && !isBye(toRemoveFrom.getRivalB())){
                toRemoveFrom.setFinished(Boolean.FALSE); 
            }
            if(toRemoveFrom.getPeriod() - 1 == getRootIndex(elimination) ){
                updateGame(toRemoveFrom);
            }else{
                save(toRemoveFrom);
            }
        }
    }
}
