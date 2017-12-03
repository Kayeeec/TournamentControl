package cz.tournament.control.service;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Swiss;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public CombinedService(CombinedRepository combinedRepository, UserRepository userRepository, SetSettingsService setSettingsService, AllVersusAllService allVersusAllService, EliminationService eliminationService, SwissService swissService) {
        this.combinedRepository = combinedRepository;
        this.userRepository = userRepository;
        this.setSettingsService = setSettingsService;
        this.allVersusAllService = allVersusAllService;
        this.eliminationService = eliminationService;
        this.swissService = swissService;
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
                //maxPlayingfields
                if(combined.getInGroupTournamentType() == TournamentType.ALL_VERSUS_ALL 
                        || combined.getInGroupTournamentType() == TournamentType.SWISS){
                    Integer playingFields = combinedDTO.getGroupSettings().getPlayingFields().get(group);
                    int max = (int) Math.floor(participantList.size()/2);
                    if( playingFields < 1 || playingFields > max ){
                        throw new IllegalArgumentException("Group "+group+" cannot have "+playingFields+" playing field/s.");
                    }
                }
            }
        }
        // #participants < #groups * #winnersToPlayoff => fail
        if(combined.getAllParticipants().size() < combined.getNumberOfGroups() * combined.getNumberOfWinnersToPlayoff()){
            String msg = "Not enough participants with respect to number of groups and number of participants to playoff."
                    + "#participants("+combined.getAllParticipants().size()+") < #groups("+combined.getNumberOfGroups()+") * "
                    + "#winnersToPlayoff("+combined.getNumberOfWinnersToPlayoff()+").";
            throw new IllegalArgumentException(msg);
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
     * 
     * 
     * @param combinedDTO
     * @return 
     */
    public Combined updateCombined(CombinedDTO combinedDTO) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
            //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * 
     * 
     * @param combinedDTO
     * @return 
     */
    public Combined generatePlayoff(CombinedDTO combinedDTO) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
            //To change body of generated methods, choose Tools | Templates.
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
        groupSettings.setSetSettings(setSettingsService.save(groupSettings.getSetSettings()));
        
        int division_fields=1, remainder_fields = 1;
        if (groupSettings.getTotalPlayingFields() != null) {
            division_fields = (int) Math.floor(groupSettings.getTotalPlayingFields() / combined.getNumberOfGroups());
            remainder_fields = groupSettings.getTotalPlayingFields() % combined.getNumberOfGroups();
        }
        
        int participantIndex = 0;
        List<Participant> participants = new ArrayList<>(combined.getAllParticipants());
        int remainder = participants.size() % combined.getNumberOfGroups();
        int groupParticipantCount = (int) Math.floor(participants.size()/combined.getNumberOfGroups());
        String letter = "A";
        
        for (int i = 0; i < combined.getNumberOfGroups(); i++) {
            AllVersusAll allVersusAll = make_AllVersusAll(combinedDTO, letter);
            
            //finish resolving playing fields - resolve max in each tournament
            if(groupSettings.getTotalPlayingFields() != null && groupSettings.getTotalPlayingFields() > combined.getNumberOfGroups()){
                int fields_result = division_fields;
                if(remainder_fields > 0){
                    fields_result++; remainder_fields--;
                }
                allVersusAll.setPlayingFields(fields_result);
            }
                
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
    
    private Integer resolvePlayingFields(GroupSettingsDTO groupSettings, String group){
        if (groupSettings.getPlayingFields() != null) {
            return groupSettings.getPlayingFields().get(group); 
        }
        //total playing fields may not be null => will be set similarily to participants later in the cycle 
        return 1;
    }
    
    private Combined generate_allVersusAllGroups_withGrouping(CombinedDTO combinedDTO){
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        groupSettings.setSetSettings(setSettingsService.save(groupSettings.getSetSettings()));
        
        int division_fields=1, remainder_fields = 1;
        if (groupSettings.getTotalPlayingFields() != null) {
            division_fields = (int) Math.floor(groupSettings.getTotalPlayingFields() / combined.getNumberOfGroups());
            remainder_fields = groupSettings.getTotalPlayingFields() % combined.getNumberOfGroups();
        }
        
        for (Map.Entry<String, List<Participant>> groupEntry : combinedDTO.getGrouping().entrySet()) {
            String group = groupEntry.getKey();
            List<Participant> participantList = groupEntry.getValue();

            AllVersusAll allVersusAll = make_AllVersusAll(combinedDTO, group);
            allVersusAll.participants(new HashSet<>(participantList));
            
            //finish resolving playing fields - resolve max in each tournament
            if(groupSettings.getTotalPlayingFields() != null && groupSettings.getTotalPlayingFields() > combined.getNumberOfGroups()){
                int fields_result = division_fields;
                if(remainder_fields > 0){
                    fields_result++;
                    remainder_fields--;
                }
                allVersusAll.setPlayingFields(fields_result);
            }
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
        groupSettings.setSetSettings(setSettingsService.save(groupSettings.getSetSettings()));
        
        int division_fields=1, remainder_fields = 1;
        if (groupSettings.getTotalPlayingFields() != null) {
            division_fields = (int) Math.floor(groupSettings.getTotalPlayingFields() / combined.getNumberOfGroups());
            remainder_fields = groupSettings.getTotalPlayingFields() % combined.getNumberOfGroups();
        }
        
        for (Map.Entry<String, List<Participant>> groupEntry : combinedDTO.getGrouping().entrySet()) {
            String group = groupEntry.getKey();
            List<Participant> participantList = groupEntry.getValue();
            
            Swiss swiss = make_swiss(combinedDTO, group);
            swiss.participants(new HashSet<>(participantList));
            
            //finish resolving playing fields - resolve max in each tournament
            if(groupSettings.getTotalPlayingFields() != null && groupSettings.getTotalPlayingFields() > combined.getNumberOfGroups()){
                int fields_result = division_fields;
                if(remainder_fields > 0){
                    fields_result++;
                    remainder_fields--;
                }
                swiss.setPlayingFields(fields_result);
            }
            
            SwissDTO swissDTO = new SwissDTO(swiss, combinedDTO.getSeeding().get(group));
            combined = combined.addGroups(swissService.createSwiss(swissDTO));//sets user and created
        }
        return combined;
    }

    private Combined generate_swissGroups_noGrouping(CombinedDTO combinedDTO) {
        GroupSettingsDTO groupSettings = combinedDTO.getGroupSettings();
        Combined combined = combinedDTO.getCombined();
        groupSettings.setSetSettings(setSettingsService.save(groupSettings.getSetSettings()));
        
        int division_fields=1, remainder_fields = 1;
        if (groupSettings.getTotalPlayingFields() != null) {
            division_fields = (int) Math.floor(groupSettings.getTotalPlayingFields() / combined.getNumberOfGroups());
            remainder_fields = groupSettings.getTotalPlayingFields() % combined.getNumberOfGroups();
        }
        
        int participantIndex = 0;
        List<Participant> participants = new ArrayList<>(combined.getAllParticipants());
        int remainder = participants.size() % combined.getNumberOfGroups();
        int groupParticipantCount = (int) Math.floor(participants.size()/combined.getNumberOfGroups());
        String letter = "A";
        
        for (int i = 0; i < combined.getNumberOfGroups(); i++) {
            Swiss swiss = make_swiss(combinedDTO, letter);
            
            //finish resolving playing fields - resolve max in each tournament
            if(groupSettings.getTotalPlayingFields() != null && groupSettings.getTotalPlayingFields() > combined.getNumberOfGroups()){
                int fields_result = division_fields;
                if(remainder_fields > 0){
                    fields_result++; remainder_fields--;
                }
                swiss.setPlayingFields(fields_result);
            }
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
        groupSettings.setSetSettings(setSettingsService.save(groupSettings.getSetSettings()));
        
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
        groupSettings.setSetSettings(setSettingsService.save(groupSettings.getSetSettings()));
                
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
            .setSettings(groupSettings.getSetSettings()) //already saved
            .playingFields(resolvePlayingFields(groupSettings, name));

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
            .setSettings(groupSettings.getSetSettings()) //already saved
            .playingFields(resolvePlayingFields(groupSettings, name));
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
            .setSettings(groupSettings.getSetSettings()); //already saved

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

    
}
