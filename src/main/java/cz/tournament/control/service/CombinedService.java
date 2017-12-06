package cz.tournament.control.service;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.enumeration.EliminationType;
import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.CombinedRepository;
import cz.tournament.control.repository.UserRepository;
import cz.tournament.control.security.SecurityUtils;
import cz.tournament.control.service.dto.CombinedDTO;
import cz.tournament.control.service.dto.GroupSettingsDTO;
import cz.tournament.control.service.dto.PlayoffSettingsDTO;
import cz.tournament.control.service.dto.SwissDTO;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final UserRepository userRepository;
    private final SetSettingsService setSettingsService;
    private final AllVersusAllService allVersusAllService;
    private final EliminationService eliminationService;
    private final SwissService swissService;
    private final TournamentService tournamentService;

    public CombinedService(CombinedRepository combinedRepository, UserRepository userRepository, SetSettingsService setSettingsService, AllVersusAllService allVersusAllService, EliminationService eliminationService, SwissService swissService, TournamentService tournamentService) {
        this.combinedRepository = combinedRepository;
        this.userRepository = userRepository;
        this.setSettingsService = setSettingsService;
        this.allVersusAllService = allVersusAllService;
        this.eliminationService = eliminationService;
        this.swissService = swissService;
        this.tournamentService = tournamentService;
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
    
    private void validate(CombinedDTO combinedDTO){
        Combined combined = combinedDTO.getCombined();
        
        if(combinedDTO.getGrouping() != null){
            for (Map.Entry<String, List<Participant>> groupingEntry : combinedDTO.getGrouping().entrySet()) {
                String group = groupingEntry.getKey();
                List<Participant> participantList = groupingEntry.getValue();
                //enough players
                if(participantList.size() < combined.getNumberOfWinnersToPlayoff()){
                    throw new IllegalArgumentException("Group "+group+" does not have enough "
                            + "participants ("+participantList.size()+").");
                }
//                //maxPlayingfields - max should not be an issue 
//                if(combined.getInGroupTournamentType() == TournamentType.ALL_VERSUS_ALL 
//                        || combined.getInGroupTournamentType() == TournamentType.SWISS 
//                        && combinedDTO.getGroupSettings().getPlayingFields() != null){
//                    Integer playingFields = combinedDTO.getGroupSettings().getPlayingFields().get(group);
//                    int max = (int) Math.floor(participantList.size()/2);
//                    if( playingFields < 1 || playingFields > max ){
//                        throw new IllegalArgumentException("Group "+group+" cannot have "+playingFields+" playing field/s.");
//                    }
//                }
            }
        }
        
    }
    
    public Combined createCombined(CombinedDTO combinedDTO) {
        log.debug("Request to create CombinedDTO: {}", combinedDTO);
        Combined combined = combinedDTO.getCombined();
        combined.setUser(userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get());
        combined.setCreated(Instant.now());
        
        validate(combinedDTO);
        
        combined = generateGroups(combinedDTO);
        
        //create empty playoff - without participants => no games generated
        combined = createEmptyPlayoff(combinedDTO);
        
        return combinedRepository.save(combined);
    }
    
    /**
     * 1. validates DTO
     * 2. decides wether groups or playoff need to be regenerated and
     *      changes combinedDTO.combined accordingly
     * 3. saves combinedDTO.combined and returns the persisted entity
     * 
     * @param combinedDTO 
     * @return persiste Combined entity
     */
    public Combined updateCombined(CombinedDTO combinedDTO) {
        CombinedDTO oldCombinedDTO = parseToDTO(findOne(combinedDTO.getCombined().getId()));
        
        validate(combinedDTO);
        boolean groupsRegenerated = false;
        
        if(regenerating_groups_needed(combinedDTO, oldCombinedDTO)){
            combinedDTO.setCombined(deleteGroups(combinedDTO.getCombined()));
            combinedDTO.setCombined(generateGroups(combinedDTO));
            groupsRegenerated = true;
        }
        if (regenerating_playoff_needed(groupsRegenerated, combinedDTO, oldCombinedDTO)) {
            tournamentService.delete(combinedDTO.getCombined().getPlayoff().getId());
            combinedDTO.getCombined().setPlayoff(null);
            combinedDTO.setCombined(createEmptyPlayoff(combinedDTO));
        }
        
        return combinedRepository.save(combinedDTO.getCombined());
    }
    
    /**
     * 1. checks if all games in all groups - validateForPlayoff(combined);
     * 2. extracts desired number of winners from them 
     * 3. sets them as participants of playoff
     * 4. lets playof generate matches by updating it
     * 5. saves combined with updated and persisted playoff and returns it
     * 
     * automatic seeding for now 
     * 
     * @param id of a Combined entity to generate playof to 
     * @return Combined entity with generated playoff 
     */
    public Combined generatePlayoff(Long id) {
        if(id == null)throw new IllegalArgumentException("Given id is null.");
        Combined combined = findOne(id);
        
        validateForPlayoff(combined);
        
        Set<Participant> playoffParticipants = extractWinners(combined);
        Long playoffId = combined.getPlayoff().getId();
        
        switch (combined.getPlayoffType()) {
            case ALL_VERSUS_ALL:
                    AllVersusAll allVersusAll = allVersusAllService.findOne(playoffId);
                    allVersusAll.setParticipants(playoffParticipants);
                    combined.setPlayoff(allVersusAllService.updateAllVersusAll(allVersusAll));
                    return save(combined);
            case SWISS:
                Swiss swiss = swissService.findOne(playoffId);
                swiss.setParticipants(playoffParticipants);
                SwissDTO swissDTO = new SwissDTO(swiss);
                combined.setPlayoff(swissService.updateSwiss(swissDTO));
                return save(combined);
            default://elimination single or double, type already set
                Elimination elimination = eliminationService.findOne(playoffId);
                elimination.setParticipants(playoffParticipants);
                combined.setPlayoff(eliminationService.updateElimination(elimination, null));
                return save(combined);    
        } 
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

    private Combined generateGroups(CombinedDTO combinedDTO) {
        combinedDTO = resolveFields(combinedDTO);
        SetSettings setSettings = combinedDTO.getGroupSettings().getSetSettings();
        if(setSettings != null){
            combinedDTO.getGroupSettings().setSetSettings(setSettingsService.save(setSettings));
        }
        
        switch (combinedDTO.getCombined().getInGroupTournamentType()){
            case ALL_VERSUS_ALL:
                return generate_allVersusAllGroups(combinedDTO);
            case SWISS:
                return generate_swissGroups(combinedDTO);
            default: //elimination single and double
                return generate_eliminationGroups(combinedDTO); 
        }
    }
    
    private Combined generate_allVersusAllGroups_noGrouping(CombinedDTO combinedDTO){
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
                
        int participantIndex = 0;
        List<Participant> participants = new ArrayList<>(combined.getAllParticipants());
        int remainder = participants.size() % combined.getNumberOfGroups();
        int groupParticipantCount = (int) Math.floor(participants.size()/combined.getNumberOfGroups());
        String letter = "A";
        
        for (int i = 0; i < combined.getNumberOfGroups(); i++) {
            AllVersusAll allVersusAll = make_AllVersusAll(combinedDTO, letter);
            
            //add #groupParticipantCount participants
            for (int p = 0; p < groupParticipantCount; p++) {
                Participant participant = participants.get(participantIndex);
                allVersusAll.addParticipants(participant);
                participantIndex++;
            }
            //+1 if remainder > 0
            if(remainder > 0){
                allVersusAll.addParticipants(participants.get(participantIndex));
                participantIndex++;
                remainder--;
            }
            
            combined = combined.addGroups(allVersusAllService.createAllVersusAll(allVersusAll));//sets user and created
            letter = getNextLetter(letter);
        }
        return combined;
    }
    
    
    private Combined generate_allVersusAllGroups_withGrouping(CombinedDTO combinedDTO){
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        
        for (Map.Entry<String, List<Participant>> groupEntry : combinedDTO.getGrouping().entrySet()) {
            String group = groupEntry.getKey();
            List<Participant> participantList = groupEntry.getValue();

            AllVersusAll allVersusAll = make_AllVersusAll(combinedDTO, group);
            allVersusAll.participants(new HashSet<>(participantList));
            
            combined = combined.addGroups(allVersusAllService.createAllVersusAll(allVersusAll));//sets user and created
        }
        return combined;
    }

    private Combined generate_allVersusAllGroups(CombinedDTO combinedDTO) {
        if(combinedDTO.getGrouping() != null){
            return generate_allVersusAllGroups_withGrouping(combinedDTO);
        }
        return generate_allVersusAllGroups_noGrouping(combinedDTO);
    }
       
    
    private Combined generate_swissGroups(CombinedDTO combinedDTO) {
        if(combinedDTO.getGrouping() != null){
            return generate_swissGroups_withGrouping(combinedDTO);
        }
        return generate_swissGroups_noGrouping(combinedDTO);
        
    }

    private Combined generate_eliminationGroups(CombinedDTO combinedDTO) {
        if(combinedDTO.getGrouping() != null){
            return generate_eliminationGroups_withGrouping(combinedDTO);
        }
        return generate_eliminationGroups_noGrouping(combinedDTO);
    }

    private String getNextLetter(String letter) {
        int charValue = letter.charAt(0);
        return String.valueOf((char) (charValue + 1));
    }

    private Combined generate_swissGroups_withGrouping(CombinedDTO combinedDTO) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        
        for (Map.Entry<String, List<Participant>> groupEntry : combinedDTO.getGrouping().entrySet()) {
            String group = groupEntry.getKey();
            List<Participant> participantList = groupEntry.getValue();
            
            Swiss swiss = make_swiss(combinedDTO, group);
            swiss.participants(new HashSet<>(participantList));
            
            SwissDTO swissDTO = new SwissDTO(swiss);
            if(combinedDTO.getSeeding() != null){
                swissDTO.setSeeding(combinedDTO.getSeeding().get(group));
            }
            combined = combined.addGroups(swissService.createSwiss(swissDTO));//sets user and created
        }
        return combined;
    }

    private Combined generate_swissGroups_noGrouping(CombinedDTO combinedDTO) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
                
        int participantIndex = 0;
        List<Participant> participants = new ArrayList<>(combined.getAllParticipants());
        int remainder = participants.size() % combined.getNumberOfGroups();
        int groupParticipantCount = (int) Math.floor(participants.size()/combined.getNumberOfGroups());
        String letter = "A";
        
        for (int i = 0; i < combined.getNumberOfGroups(); i++) {
            Swiss swiss = make_swiss(combinedDTO, letter);
            
            //add #groupParticipantCount participants
            for (int p = 0; p < groupParticipantCount; p++) {
                Participant participant = participants.get(participantIndex);
                swiss.addParticipants(participant);
                participantIndex++;
            }
            //+1 if remainder > 0
            if(remainder > 0){
                swiss.addParticipants(participants.get(participantIndex));
                participantIndex++;
                remainder--;
            }
            SwissDTO swissDTO = new SwissDTO(swiss, null);//no custom seeding either 
            combined = combined.addGroups(swissService.createSwiss(swissDTO));//sets user and created
            letter = getNextLetter(letter);
        }
        return combined;
    }
    
    private Combined generate_eliminationGroups_withGrouping(CombinedDTO combinedDTO) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        
        for (Map.Entry<String, List<Participant>> groupEntry : combinedDTO.getGrouping().entrySet()) {
            String group = groupEntry.getKey();
            List<Participant> participantList = groupEntry.getValue();
            
            Elimination elimination =  make_elimination(combinedDTO, group);
            elimination.participants(new HashSet<>(participantList));
            
            combined = combined.addGroups(
                    eliminationService.createElimination(elimination, combinedDTO.getSeeding().get(group))
            );//sets user and created
        }
        return combined;
    }

    private Combined generate_eliminationGroups_noGrouping(CombinedDTO combinedDTO) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
                
        int participantIndex = 0;
        List<Participant> participants = new ArrayList<>(combined.getAllParticipants());
        int remainder = participants.size() % combined.getNumberOfGroups();
        int groupParticipantCount = (int) Math.floor(participants.size()/combined.getNumberOfGroups());
        String letter = "A";
        
        for (int i = 0; i < combined.getNumberOfGroups(); i++) {
            Elimination elimination = make_elimination(combinedDTO, letter);
            
            //add #groupParticipantCount participants
            for (int p = 0; p < groupParticipantCount; p++) {
                Participant participant = participants.get(participantIndex);
                elimination.addParticipants(participant);
                participantIndex++;
            }
            //+1 if remainder > 0
            if(remainder > 0){
                elimination.addParticipants(participants.get(participantIndex));
                participantIndex++;
                remainder--;
            }
            combined = combined.addGroups(eliminationService.createElimination(elimination, null));//sets user and created
            letter = getNextLetter(letter);
        }
        return combined;
    }
    
    /**
     * - without setting participants 
     * - expects saved setSettings on groupSettings
     * 
     * - important to finish playing fields
     * 
     * @param combinedDTO
     * @param name - group letter or 'playoff'
     * @return 
     */
    private AllVersusAll make_AllVersusAll(CombinedDTO combinedDTO, String name) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        
        AllVersusAll allVersusAll = new AllVersusAll();
        allVersusAll
            .numberOfMutualMatches(groupSettings.getNumberOfMutualMatches())
            .name(name)
//            .user(combined.getUser())
//            .created(combined.getCreated().atZone(ZoneId.systemDefault()))
            .inCombined(Boolean.TRUE)
            .pointsForWinning(groupSettings.getPointsForWinning())
            .pointsForLosing(groupSettings.getPointsForLosing())
            .pointsForTie(groupSettings.getPointsForTie())
            .setsToWin(groupSettings.getSetsToWin())
            .tiesAllowed(groupSettings.getTiesAllowed())
            .setSettings(groupSettings.getSetSettings()) 
            .playingFields(groupSettings.getPlayingFields().get(name));

        return allVersusAll;
    }
    
    /**
     * - without setting participants 
     * - expects saved setSettings on groupSettings
     * 
     * - important to finish playing fields
     * 
     * @param combinedDTO
     * @param name - group letter or 'playoff'
     * @return 
     */
    private Swiss make_swiss(CombinedDTO combinedDTO, String name) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        
        Swiss swiss = new Swiss();
        swiss.color(groupSettings.getColor())
            .name(name)
//            .user(combined.getUser())
//            .created(combined.getCreated().atZone(ZoneId.systemDefault()))
            .inCombined(Boolean.TRUE)
            .pointsForWinning(groupSettings.getPointsForWinning())
            .pointsForLosing(groupSettings.getPointsForLosing())
            .pointsForTie(groupSettings.getPointsForTie())
            .setsToWin(groupSettings.getSetsToWin())
            .tiesAllowed(groupSettings.getTiesAllowed())
            .setSettings(groupSettings.getSetSettings())
            .playingFields(groupSettings.getPlayingFields().get(name));
        return swiss;
    }
    
    /**
     * - without setting participants 
     * - expects saved setSettings on groupSettings
     * 
     * @param combinedDTO
     * @param name - group letter or 'playoff'
     * @return 
     */
    private Elimination make_elimination(CombinedDTO combinedDTO, String name) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        
        Elimination elimination = new Elimination();
        elimination.bronzeMatch(groupSettings.getBronzeMatch())
            .name(name)
//            .user(combined.getUser())
//            .created(combined.getCreated().atZone(ZoneId.systemDefault()))
            .inCombined(Boolean.TRUE)
            .pointsForWinning(groupSettings.getPointsForWinning())
            .pointsForLosing(groupSettings.getPointsForLosing())
            .setsToWin(groupSettings.getSetsToWin())
            .setSettings(groupSettings.getSetSettings()); 

        if (combinedDTO.getCombined().getInGroupTournamentType() == TournamentType.ELIMINATION_SINGLE) {
            elimination.type(EliminationType.SINGLE);
        } else {//double
            elimination.type(EliminationType.DOUBLE);
        }
        return elimination;
    }

    private Combined createEmptyPlayoff(CombinedDTO combinedDTO) {
        switch (combinedDTO.getCombined().getPlayoffType()) {
            case ALL_VERSUS_ALL:
                return createEmpty_allVersusAll_playoff(combinedDTO);
            case SWISS:
                return createEmpty_swiss_playoff(combinedDTO);
            default: //elimination single and double 
                return createEmpty_elimination_playoff(combinedDTO);
        }
    }

    private Combined createEmpty_allVersusAll_playoff(CombinedDTO combinedDTO) {
        PlayoffSettingsDTO playoffSettings = combinedDTO.getPlayoffSettings();
        Combined combined = combinedDTO.getCombined();
        playoffSettings.setSetSettings(setSettingsService.save(playoffSettings.getSetSettings()));
        
        String name = combined.getName() + " - playoff";
        
        AllVersusAll playoff = new AllVersusAll().numberOfMutualMatches(playoffSettings.getNumberOfMutualMatches());
        playoff
            .name(name)
            .inCombined(Boolean.TRUE)
            .pointsForWinning(playoffSettings.getPointsForWinning())
            .pointsForLosing(playoffSettings.getPointsForLosing())
            .pointsForTie(playoffSettings.getPointsForTie())
            .setsToWin(playoffSettings.getSetsToWin())
            .tiesAllowed(playoffSettings.getTiesAllowed())
            .setSettings(playoffSettings.getSetSettings()) //already saved
            .playingFields(playoffSettings.getPlayingFields());
        //not setting participants 
        combined.setPlayoff(allVersusAllService.createAllVersusAll(playoff)); //sets user and created
        return combined;
    }

    private Combined createEmpty_swiss_playoff(CombinedDTO combinedDTO) {
        PlayoffSettingsDTO playoffSettings = combinedDTO.getPlayoffSettings();
        Combined combined = combinedDTO.getCombined();
        playoffSettings.setSetSettings(setSettingsService.save(playoffSettings.getSetSettings()));
        
        String name = combined.getName() + " - playoff";
        
        Swiss playoff = new Swiss().color(playoffSettings.getColor());
        playoff
            .playingFields(playoffSettings.getPlayingFields())
            .setSettings(playoffSettings.getSetSettings()) //already saved
            .tiesAllowed(playoffSettings.getTiesAllowed())
            .name(name)
            .inCombined(Boolean.TRUE)
            .pointsForWinning(playoffSettings.getPointsForWinning())
            .pointsForLosing(playoffSettings.getPointsForLosing())
            .pointsForTie(playoffSettings.getPointsForTie())
            .setsToWin(playoffSettings.getSetsToWin());
        
        //not setting participants 
        
        SwissDTO swissDTO = new SwissDTO(playoff, null);
        combined.setPlayoff(swissService.createSwiss(swissDTO)); //sets user and created
        return combined;
    }

    private Combined createEmpty_elimination_playoff(CombinedDTO combinedDTO) {
        PlayoffSettingsDTO playoffSettings = combinedDTO.getPlayoffSettings();
        Combined combined = combinedDTO.getCombined();
        playoffSettings.setSetSettings(setSettingsService.save(playoffSettings.getSetSettings()));
        
        String name = combined.getName() + " - playoff";
        
        Elimination playoff = new Elimination().bronzeMatch(playoffSettings.getBronzeMatch());
        playoff
            .name(name)
            .user(combined.getUser())
            .created(combined.getCreated().atZone(ZoneId.systemDefault()))
            .inCombined(Boolean.TRUE)
            .pointsForWinning(playoffSettings.getPointsForWinning())
            .pointsForLosing(playoffSettings.getPointsForLosing())
            .setsToWin(playoffSettings.getSetsToWin())
            .setSettings(playoffSettings.getSetSettings()); //already saved
                
            if (combinedDTO.getCombined().getInGroupTournamentType() == TournamentType.ELIMINATION_SINGLE) {
                playoff = playoff.type(EliminationType.SINGLE);
            } else {//double
               playoff = playoff.type(EliminationType.DOUBLE);
            }
            //no participants
            combined.setPlayoff(eliminationService.createElimination(playoff, null));
            return combined;     
    }

    private CombinedDTO parseToDTO(Combined oldCombined) {
        CombinedDTO result = new CombinedDTO(oldCombined);
        
        //set grouping and seeding
        Map<String,List<Participant>> grouping = new HashMap<>();
        Map<String,List<Participant>> seeding = new HashMap<>();
        
        if(oldCombined.getInGroupTournamentType() == TournamentType.ALL_VERSUS_ALL){
            seeding = null;
            for (Tournament group : oldCombined.getGroups()) {
                grouping.put(group.getName(), new ArrayList<>(group.getParticipants()));
            }
        }else{
            for (Tournament group : oldCombined.getGroups()) {
                grouping.put(group.getName(), new ArrayList<>(group.getParticipants()));
                seeding.put(group.getName(), getSeeding(oldCombined, group));
            }
        }
        result.setGrouping(grouping);
        result.setSeeding(seeding);
        return result;
    }
    
    
    /**
     * +=============================+======+==============================================+
     * |    CHANGED ATTRIBUTE        |      | CAUSES DELETE AND RECREATE TO                |
     * +=============================+======+==============================================+
     * | - grouping                  |      |       groups                                 |
     * | - seeding                   |      |       groups                                 |
     * +------GROUP SETTINGS---------+------+----------------------------------------------|
     * | - pointsForWinning;         |      |                                              |
     * | - pointsForTie;             |      |                                              |
     * | - pointsForLosing;          |      |                                              |
     * | - setsToWin;                |      |       -TODO recoputeSetsToWin()              |
     * | - setSettings;              |      |                                              |
     * | - color;                    | S--  |       groups                                 |
     * | - numberOfMutualMatches;    | -A-  |       groups                                 |
     * | - playingFields;            | SA-  |       TODO make recomputePlayingFields()     |
     * | - totalPlayingFields;       | SA-  |       -"-                                    |
     * | - tiesAllowed;              | SA-  |                                              |
     * | - bronzeMatch;              | --E  |       groups                                 |
     * +------PLAYOFF SETTINGS-------+------+----------------------------------------------+
     * | - pointsForWinning;         |      |                                              |
     * | - pointsForTie;             |      |                                              |
     * | - pointsForLosing;          |      |                                              |
     * | - setsToWin;                |      |       -recomputeSetsToWin()?                 |
     * | - setSettings;              |      |                                              |
     * | - color;                    | S--  |       playoff                                |
     * | - numberOfMutualMatches;    | -A-  |       playoff                                |
     * | - playingFields;            | SA-  |       TODO make recomputePlayingFields()     |
     * | - tiesAllowed;              | SA-  |                                              |
     * | - bronzeMatch;              | --E  |       playoff                                |
     * +------COMBINED---------------+------+----------------------------------------------+
     * | - numberOfWinnersToPlayoff  |      |       playoff if it has participants         |
     * | - numberOfGroups            |      |       groups                                 |
     * | - playoffType               |      |       playoff                                |
     * | - inGroupTournamentType     |      |       groups                                 |
     * | - allParticipants           |      |       groups, playoff if it has participants |
     * +-----------------------------+------+----------------------------------------------+
     * @param combinedDTO
     * @param oldCombinedDTO
     * @return 
     */
    private boolean regenerating_groups_needed(CombinedDTO combinedDTO, CombinedDTO oldCombinedDTO) {
        return Objects.equals(combinedDTO.getGrouping(),oldCombinedDTO.getGrouping())
                && !Objects.equals(combinedDTO.getSeeding(),oldCombinedDTO.getSeeding())
                
                && !Objects.equals(combinedDTO.getGroupSettings().getColor(),oldCombinedDTO.getGroupSettings().getColor())
                && !Objects.equals(combinedDTO.getGroupSettings().getNumberOfMutualMatches(),oldCombinedDTO.getGroupSettings().getNumberOfMutualMatches())
                && !Objects.equals(combinedDTO.getGroupSettings().getBronzeMatch(),oldCombinedDTO.getGroupSettings().getBronzeMatch())
                   
                && !Objects.equals(combinedDTO.getCombined().getNumberOfGroups(),oldCombinedDTO.getCombined().getNumberOfGroups())
                && !Objects.equals(combinedDTO.getCombined().getInGroupTournamentType(),oldCombinedDTO.getCombined().getInGroupTournamentType())
                && !Objects.equals(combinedDTO.getCombined().getAllParticipants(),oldCombinedDTO.getCombined().getAllParticipants());
    }
    private boolean regenerating_playoff_needed(boolean groupsRegenerated, CombinedDTO combinedDTO, CombinedDTO oldCombinedDTO) {
        if(!combinedDTO.getCombined().getPlayoff().getParticipants().isEmpty()){
            return groupsRegenerated || 
                    !Objects.equals(combinedDTO.getCombined().getNumberOfWinnersToPlayoff(),oldCombinedDTO.getCombined().getNumberOfWinnersToPlayoff());
        }
        return !Objects.equals(combinedDTO.getPlayoffSettings().getColor(),oldCombinedDTO.getPlayoffSettings().getColor())
                && !Objects.equals(combinedDTO.getPlayoffSettings().getNumberOfMutualMatches(),oldCombinedDTO.getPlayoffSettings().getNumberOfMutualMatches())
                && !Objects.equals(combinedDTO.getPlayoffSettings().getBronzeMatch(),oldCombinedDTO.getPlayoffSettings().getBronzeMatch())
                
                && !Objects.equals(combinedDTO.getCombined().getPlayoffType(),oldCombinedDTO.getCombined().getPlayoffType());
    }


    private Combined deleteGroups(Combined combined) {
        tournamentService.delete(combined.getGroups());
        combined.setGroups(new HashSet<>());
        return combined;
    }

    private List<Participant> getSeeding(Combined oldCombined, Tournament group) {
        switch (oldCombined.getInGroupTournamentType()) {
            case ALL_VERSUS_ALL:
                return null;
            case SWISS:
                return swissService.getSwissSeeding(group.getId());
            default:
                return eliminationService.getEliminationSeeding(group.getId());
        }
    }
    
    /**
     * all games in all groups finished 
     * @param combined 
     */
    private void validateForPlayoff(Combined combined) {
        for (Tournament group : combined.getGroups()) {
            if(!group.allMatchesFinished()){
                throw new IllegalArgumentException("Cannot generate playoff, group "+group.getName()+" is not finished.");
            }
        }
    }
    
    /**
     * expects all groups to be finished
     * @param combined
     * @return 
     */
    private Set<Participant> extractWinners(Combined combined) {
        Integer n = combined.getNumberOfWinnersToPlayoff();
        Set<Participant> result = new HashSet<>();
        
        for (Tournament group : combined.getGroups()) {
            result.addAll(group.getNWinners(n));
        }
        return result;
    }

    //expects grouping
    private CombinedDTO resolveFields_withGrouping(CombinedDTO combinedDTO) {
        Integer total = combinedDTO.getGroupSettings().getTotalPlayingFields();
        Integer groupsNum = combinedDTO.getCombined().getNumberOfGroups();
        
        Map<String,Integer> idealsMap = new HashMap<>();
        int idealTotal = 0;
        
        //compute idealTotal and idealsMap
        for (Map.Entry<String, List<Participant>> group : combinedDTO.getGrouping().entrySet()) {
            String groupName = group.getKey();
            List<Participant> groupParticipants = group.getValue();
            
            int ideal = (int) Math.floor(groupParticipants.size()/2);
            idealTotal +=ideal;
            idealsMap.put(groupName, ideal);
        }
        
        Map<String,Integer> fieldMap = new HashMap<>();
        
        if(total < idealTotal){
            // participant-like algorithm on sorted map 
            idealsMap = sortByValue_descending(idealsMap); //sorted
            int remainder = total % groupsNum;
            int division = total/groupsNum;
            
            for (Map.Entry<String, Integer> entry : idealsMap.entrySet()) {
                String group = entry.getKey();
                int fields = division;
                if(remainder > 0){
                    fields += 1;
                    remainder--;
                }
                fieldMap.put(group, fields);
            }
            
            combinedDTO.getGroupSettings().setPlayingFields(fieldMap);
            return combinedDTO; 
        }
        //just return with ideals
        combinedDTO.getGroupSettings().setPlayingFields(idealsMap);
        return combinedDTO;
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue_descending(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list
                = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;

    }

    private CombinedDTO resolveFields_noGrouping(CombinedDTO combinedDTO) {
        Map<String,Integer> idealsMap = new HashMap();
        
        int allParticipantsNum = combinedDTO.getCombined().getAllParticipants().size();
        Integer groupsNum = combinedDTO.getCombined().getNumberOfGroups();
        int participantDivider = allParticipantsNum / groupsNum;
        int participantRemainder = allParticipantsNum % groupsNum;
        
        
        int idealTotal = 0;
        
        //couts idealTotal, participantsCountMap and idealsMap
        String letter = "A";
        for (int i = 0; i < groupsNum; i++) {
            int participantCount = participantDivider;
            if(participantRemainder > 0){
                participantCount += 1;
                participantRemainder--;
            }            
            int ideal = (int) Math.floor(participantCount/2);
            idealTotal +=ideal;
            idealsMap.put(letter, ideal);
            
            letter = getNextLetter(letter);
        }
        
        Map<String,Integer> fieldMap = new HashMap();
        Integer total = combinedDTO.getGroupSettings().getTotalPlayingFields();
        if(total < idealTotal){
            // participant-like algorithm on sorted map 
            idealsMap = sortByValue_descending(idealsMap); //sorted
            int remainder = total % groupsNum;
            int division = total/groupsNum;
            
            for (Map.Entry<String, Integer> entry : idealsMap.entrySet()) {
                String group = entry.getKey();
                int fields = division;
                if(remainder > 0){
                    fields += 1;
                    remainder--;
                }
                fieldMap.put(group, fields);
            }
            
            combinedDTO.getGroupSettings().setPlayingFields(fieldMap);
            return combinedDTO; 
        }
        //just return with ideals
        combinedDTO.getGroupSettings().setPlayingFields(idealsMap);
        return combinedDTO;
    }
    
    /**
     * either fieldsTotal is null or map playing fields is null in dto
     * initialyzed in front end 
     * 
     * @param combinedDTO
     * @return 
     */
    private CombinedDTO resolveFields(CombinedDTO combinedDTO) {
        if(combinedDTO.getGroupSettings().getPlayingFields() == null 
                && combinedDTO.getGroupSettings().getTotalPlayingFields() == null ){
            return initBasicPlayingFieldsMap(combinedDTO);
        }
        if(combinedDTO.getGroupSettings().getTotalPlayingFields() == null
                || combinedDTO.getCombined().getInGroupTournamentType() == TournamentType.ELIMINATION_DOUBLE
                || combinedDTO.getCombined().getInGroupTournamentType() == TournamentType.ELIMINATION_SINGLE){
            return combinedDTO;
        }
            
        if(combinedDTO.getGrouping() != null){
            return resolveFields_withGrouping(combinedDTO);
        }
        return resolveFields_noGrouping(combinedDTO);
    }

    private CombinedDTO initBasicPlayingFieldsMap(CombinedDTO combinedDTO) {
        Map<String,Integer> playingFields = new HashMap<>();
        String letter = "A";
        for (int i = 0; i < combinedDTO.getCombined().getNumberOfGroups(); i++) {
            playingFields.put(letter, 1);
            letter = getNextLetter(letter);
        }
        combinedDTO.getGroupSettings().setPlayingFields(playingFields);
        return combinedDTO;
                
    }

    

    
}
