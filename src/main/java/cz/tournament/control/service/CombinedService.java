package cz.tournament.control.service;

import cz.tournament.control.domain.AllVersusAll;
import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Player;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.domain.Team;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.enumeration.EliminationType;
import cz.tournament.control.domain.enumeration.TournamentType;
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
    private final ParticipantService participantService;

    public CombinedService(CombinedRepository combinedRepository, UserRepository userRepository, SetSettingsService setSettingsService, AllVersusAllService allVersusAllService, EliminationService eliminationService, SwissService swissService, TournamentService tournamentService, ParticipantService participantService) {
        this.combinedRepository = combinedRepository;
        this.userRepository = userRepository;
        this.setSettingsService = setSettingsService;
        this.allVersusAllService = allVersusAllService;
        this.eliminationService = eliminationService;
        this.swissService = swissService;
        this.tournamentService = tournamentService;
        this.participantService = participantService;
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
                
                //enough players for each group, checked on frontend
                if(participantList.size() < combined.getNumberOfWinnersToPlayoff()){
                    throw new IllegalArgumentException("Group "+group+" does not have enough "
                            + "participants ("+participantList.size()+").");
                }
                
                //valid seeding for each group - keys should be the same
                if(combinedDTO.getSeeding() != null){
                    List<Participant> seeding = combinedDTO.getSeeding().get(group);
                    validateSeeding(seeding);
                }
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
        log.debug("Request to update Combined: {}", combinedDTO.getCombined());
        CombinedDTO oldCombinedDTO = parseToDTO(findOne(combinedDTO.getCombined().getId()));
        
        validate(combinedDTO);
        boolean groupsRegenerated = false;
        boolean playoffRegenerated = false;
        if(regenerating_groups_needed(combinedDTO, oldCombinedDTO)){
            log.debug("updateCombined() - regenerating groups.");
            combinedDTO.setCombined(deleteGroups(combinedDTO.getCombined()));
            combinedDTO.setCombined(generateGroups(combinedDTO));
            groupsRegenerated = true;
        }
        if (regenerating_playoff_needed(groupsRegenerated, combinedDTO, oldCombinedDTO)) {
            log.debug("updateCombined() - regenerating playoff.");
            Long idOfPlayoffToDelete = combinedDTO.getCombined().getPlayoff().getId();
            
            Combined combined = combinedDTO.getCombined();
            combined.setPlayoff(null);
            combinedDTO.setCombined(save(combined));
            
            tournamentService.delete(idOfPlayoffToDelete);
            
            combinedDTO.setCombined(createEmptyPlayoff(combinedDTO));
            playoffRegenerated = true;
        }
        if(!groupsRegenerated && updating_groups_needed(combinedDTO, oldCombinedDTO)){
            combinedDTO.setCombined(updateGroups(combinedDTO));
        }
        if(!playoffRegenerated && updating_playoff_needed(combinedDTO, oldCombinedDTO)){
            combinedDTO.setCombined(updatePlayoff(combinedDTO));
        }
        
        return combinedRepository.save(combinedDTO.getCombined());
    }
    
    /**
     * 1. checks if all games in all groups are finished - validateForPlayoff(combined);
     * 2. extracts desired number of winners from groups 
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
        
        List<Participant> seeding = preparePlayoffSeeding(combined);
        
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
                SwissDTO swissDTO = new SwissDTO().swiss(swiss).seeding(seeding);
                combined.setPlayoff(swissService.updateSwiss(swissDTO));
                return save(combined);
            default://elimination single or double, type already set
                Elimination elimination = eliminationService.findOne(playoffId);
                elimination.setParticipants(playoffParticipants);
                combined.setPlayoff(eliminationService.updateElimination(elimination, seeding));
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
    
    @Transactional(readOnly = true)
    public List<Combined> findByParticipant(Participant participant) {
        log.debug("Request to get all Combineds for participant {}", participant);
        return combinedRepository.findByAllParticipantsContains(participant);
    }
    
    @Transactional(readOnly = true)
    public List<Combined> findByTeam(Team team) {
        log.debug("Request to get all Combineds for team {}", team);
        return combinedRepository.findByAllParticipantsContains(participantService.findByTeam(team));
    }
    
    @Transactional(readOnly = true)
    public List<Combined> findByPlayer(Player player) {
        log.debug("Request to get all Combineds for player {}", player);
        return combinedRepository.findByAllParticipantsContains(participantService.findByPlayer(player));
    }
    
    @Transactional(readOnly = true)
    public Combined findByTournament(Tournament tournament) {
        log.debug("Request to get all Combineds for tournament {}", tournament);
        if(tournament.getName().length() == 1){ //all groups have one letter name 
            return combinedRepository.findByGroupsContains(tournament).get(0);
        }
        return combinedRepository.findByPlayoff(tournament);
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
        return combinedRepository.findOneWithEagerRelationships(id);
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
     * +------------------------------------------+-----------------------------------------------------+
     * | combinedDTO:                                                                                   |
     * +------------------------------------------+-----------------------------------------------------+
     * | combined.numberOfWinnersToPlayoff        |  if playoff.participants not empty -> reg. playoff  |
     * |         .numberOfGroups                  |  regenerate groups                                  |
     * |         .allParticipants                 |  regenerate groups and playoff                      |
     * |         .playoffType                     |  reg. playoff                                       |
     * |         .playoff                         |                                                     |
     * |         .inGroupTournamentType           |  reg. groups                                        |
     * |         .groups                          |                                                     |
     * +------------------------------------------+-----------------------------------------------------+
     * | groupSettings.pointsForWinning;          |  trigger update on groups                           |
     * |              .pointsForTie;              |  trigger update on groups                           |
     * |              .pointsForLosing;           |  trigger update on groups                           |
     * |              .setsToWin;                 |  trigger update on groups                           |
     * |              .setSettings;               |                                                     |
     * |              .color;                     |  trigger update on groups                           |
     * |              .numberOfMutualMatches;     |  trigger update on groups                           |
     * |              .playingFields;             |  trigger update on groups                           |
     * |              .totalPlayingFields;        |  trigger update on groups                           |
     * |              .tiesAllowed;               |  trigger update on groups                           |
     * |              .bronzeMatch;               |  trigger update on groups                           |
     * +------------------------------------------+-----------------------------------------------------+
     * | playoffSettings.pointsForWinning;        |  trigger update on playoff                          |
     * |                .pointsForTie;            |  trigger update on playoff                          |
     * |                .pointsForLosing;         |  trigger update on playoff                          |
     * |                .setsToWin;               |  trigger update on playoff                          |
     * |                .setSettings;             |                                                     |
     * |                .color;                   |  trigger update on playoff                          |
     * |                .numberOfMutualMatches;   |  trigger update on playoff                          |
     * |                .playingFields;           |  trigger update on playoff                          |
     * |                .tiesAllowed;             |  trigger update on playoff                          |
     * |                .bronzeMatch;             |  trigger update on playoff                          |
     * +------------------------------------------+-----------------------------------------------------+
     * | grouping                                 |  reg. groups, playoff if not empty                  |
     * | seeding                                  |  reg. groups, playoff if not empty                  |
     * +------------------------------------------+-----------------------------------------------------+
     * @param combinedDTO
     * @param oldCombinedDTO
     * @return 
     */
    private boolean regenerating_groups_needed(CombinedDTO combinedDTO, CombinedDTO oldCombinedDTO) {
        return      !Objects.equals(combinedDTO.getCombined().getNumberOfGroups(),          oldCombinedDTO.getCombined().getNumberOfGroups())
                ||  !Objects.equals(combinedDTO.getCombined().getAllParticipants(),         oldCombinedDTO.getCombined().getAllParticipants())
                ||  !Objects.equals(combinedDTO.getCombined().getInGroupTournamentType(),   oldCombinedDTO.getCombined().getInGroupTournamentType())
                ||  !Objects.equals(combinedDTO.getGrouping(),   oldCombinedDTO.getGrouping())
                ||  !Objects.equals(combinedDTO.getSeeding(),   oldCombinedDTO.getSeeding())
                ;
    }
    private boolean updating_groups_needed(CombinedDTO combinedDTO, CombinedDTO oldCombinedDTO){
        return     !Objects.equals(combinedDTO.getGroupSettings().getPointsForWinning(),    oldCombinedDTO.getGroupSettings().getPointsForWinning())
                || !Objects.equals(combinedDTO.getGroupSettings().getPointsForLosing(),     oldCombinedDTO.getGroupSettings().getPointsForLosing())
                || !Objects.equals(combinedDTO.getGroupSettings().getPointsForTie(),        oldCombinedDTO.getGroupSettings().getPointsForTie())
                || !Objects.equals(combinedDTO.getGroupSettings().getSetsToWin(),           oldCombinedDTO.getGroupSettings().getSetsToWin())
                || !Objects.equals(combinedDTO.getGroupSettings().getColor(),                   oldCombinedDTO.getGroupSettings().getColor())
                || !Objects.equals(combinedDTO.getGroupSettings().getNumberOfMutualMatches(),   oldCombinedDTO.getGroupSettings().getNumberOfMutualMatches())
                || !Objects.equals(combinedDTO.getGroupSettings().getBronzeMatch(),             oldCombinedDTO.getGroupSettings().getBronzeMatch())
                || !Objects.equals(combinedDTO.getGroupSettings().getTiesAllowed(),         oldCombinedDTO.getGroupSettings().getTiesAllowed())
                || !Objects.equals(combinedDTO.getGroupSettings().getPlayingFields(),       oldCombinedDTO.getGroupSettings().getPlayingFields())
                || !Objects.equals(combinedDTO.getGroupSettings().getTotalPlayingFields(),  oldCombinedDTO.getGroupSettings().getTotalPlayingFields())
                ;
    }
    private boolean regenerating_playoff_needed(boolean groupsRegenerated, CombinedDTO combinedDTO, CombinedDTO oldCombinedDTO) {
        if(!combinedDTO.getCombined().getPlayoff().getParticipants().isEmpty()){
            return groupsRegenerated || 
                    !Objects.equals(combinedDTO.getCombined().getNumberOfWinnersToPlayoff(),oldCombinedDTO.getCombined().getNumberOfWinnersToPlayoff());
        }
        return !Objects.equals(combinedDTO.getCombined().getPlayoffType(),  oldCombinedDTO.getCombined().getPlayoffType());
    }
    private boolean updating_playoff_needed(CombinedDTO combinedDTO, CombinedDTO oldCombinedDTO){
        return     !Objects.equals(combinedDTO.getPlayoffSettings().getPointsForWinning(),    oldCombinedDTO.getPlayoffSettings().getPointsForWinning())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getPointsForLosing(),     oldCombinedDTO.getPlayoffSettings().getPointsForLosing())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getPointsForTie(),        oldCombinedDTO.getPlayoffSettings().getPointsForTie())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getSetsToWin(),           oldCombinedDTO.getPlayoffSettings().getSetsToWin())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getColor(),                   oldCombinedDTO.getPlayoffSettings().getColor())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getNumberOfMutualMatches(),   oldCombinedDTO.getPlayoffSettings().getNumberOfMutualMatches())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getBronzeMatch(),             oldCombinedDTO.getPlayoffSettings().getBronzeMatch())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getTiesAllowed(),         oldCombinedDTO.getPlayoffSettings().getTiesAllowed())
                || !Objects.equals(combinedDTO.getPlayoffSettings().getPlayingFields(),       oldCombinedDTO.getPlayoffSettings().getPlayingFields())
                ;
    }


    private Combined deleteGroups(Combined combined) {
        List<Tournament> groups = new ArrayList<>(combined.getGroups());
        
        combined.setGroups(new HashSet<>());
        Combined saved = save(combined);
        
        //try to just delete stuff by id, replace with orphan removal? 
        for (Tournament group : groups) {
            tournamentService.delete(group.getId());
        }
        
        return saved;
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

    /**
     * expects grouping
     * each group can have different number of participants
     * 
     * @param combinedDTO
     * @return 
     */
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
    
    /**
     * with no grouping, participants are assigned to groups evenly 
     * @param combinedDTO
     * @return 
     */
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
    
    /**
     * map of one field for each group
     * used in case of no fields map sent from frontend
     * 
     * @param combinedDTO
     * @return 
     */
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
    
    /**
     * removes element from list and returns it 
     * 
     * @param list
     * @return null if list is empty or null, entity otherwise
     */
    private static Participant pop(List<Participant> list) {
        if(list == null || list.isEmpty()) return null;
        int index = list.size() - 1;
        Participant result = list.get(index);
        list.remove(index);
        return result;
    }
    
    /**
     * prepares seeding so that participants from the same group are not seeded together 
     * or at least not likely to do so
     * 
     * @param combined
     * @return 
     */
    private List<Participant> preparePlayoffSeeding(Combined combined) {
        if(combined.getPlayoffType() == TournamentType.ALL_VERSUS_ALL 
                || combined.getAllParticipants().size() < 2) return null;
        
        Integer wn = combined.getNumberOfWinnersToPlayoff();
        List<Participant> result = new ArrayList<>();
        int numberOfParticipants = 0; //numberOfParticipants
        
        Integer groupIndex = 0;
        Map<Integer, List<Participant>> winners = new HashMap<>();
        for (Tournament group : combined.getGroups()) {
            List<Participant> nWinners = group.getNWinners(wn);
            winners.put(groupIndex, nWinners);
            numberOfParticipants += wn;
            groupIndex++;
        }
        int numberOfByes = getNumberOfByes(numberOfParticipants, combined);
        int N = numberOfParticipants + numberOfByes;
        int byesAdded = 0;
        groupIndex = 0;
        int groupsN = combined.getNumberOfGroups();
        Participant bye = participantService.getByeParticipant();
        
        for (int i = 0; i < N/2; i++) {
            if(byesAdded < numberOfByes){
                result.add(bye);
                byesAdded++;
            }else{
                result.add(pop(winners.get( groupIndex % groupsN )));
                groupIndex++;
            }
            result.add(0, pop(winners.get( groupIndex % groupsN )));
            groupIndex++;
        }
        
        validateSeeding(result);
        
        return result;
    }
    
    private int getNumberOfByes(int n, Combined combined) {
        if (combined.getPlayoffType() == TournamentType.SWISS) {
            return n%2;
        }
        //elimination
        int N = Elimination.getNextPowerOfTwo(n);
        return N - n;
    }
    
    /**
     * no nulls and even length
     * @param result 
     */
    private void validateSeeding(List<Participant> result) {
        if(result.size() % 2 == 1){
            throw new IllegalStateException("Seeding does not have even length.");
        }
        if(result.contains(null)){
            log.debug("seeding: {}", result);
            throw new IllegalStateException("Seeding contains null.");
        }
    }

    private Combined updateGroups(CombinedDTO combinedDTO) {
        //save set settings //resolve fields
        combinedDTO = resolveFields(combinedDTO);
        SetSettings setSettings = combinedDTO.getGroupSettings().getSetSettings();
        if(setSettings != null){
            combinedDTO.getGroupSettings().setSetSettings(setSettingsService.save(setSettings));
        }
        
        switch (combinedDTO.getCombined().getInGroupTournamentType()){
            case ALL_VERSUS_ALL:
                return update_allVersusAllGroups(combinedDTO);
            case SWISS:
                return update_swissGroups(combinedDTO);
            default: //elimination single and double
                return update_eliminationGroups(combinedDTO); 
        }
    }

    private Combined update_allVersusAllGroups(CombinedDTO combinedDTO) {
        Set<AllVersusAll> saved = new HashSet<>();
        for (Tournament group : combinedDTO.getCombined().getGroups()) {
            AllVersusAll toUpdate = allVersusAllService.findOne(group.getId());
            
            toUpdate.setNumberOfMutualMatches(combinedDTO.getGroupSettings().getNumberOfMutualMatches());
            toUpdate.setPlayingFields(combinedDTO.getGroupSettings().getPlayingFields().get(group.getName()));
            toUpdate.setPointsForWinning(combinedDTO.getGroupSettings().getPointsForWinning());
            toUpdate.setPointsForLosing(combinedDTO.getGroupSettings().getPointsForLosing());
            toUpdate.setPointsForTie(combinedDTO.getGroupSettings().getPointsForTie());
            toUpdate.setSetsToWin(combinedDTO.getGroupSettings().getSetsToWin());
            toUpdate.setTiesAllowed(combinedDTO.getGroupSettings().getTiesAllowed());
            
            saved.add(allVersusAllService.updateAllVersusAll(toUpdate));
        }
        Combined combined = combinedDTO.getCombined();
        combined.getGroups().clear();
        combined.getGroups().addAll(saved);
        return combined;
    }

    private Combined update_swissGroups(CombinedDTO combinedDTO) {
        Set<Swiss> saved = new HashSet<>();
        for (Tournament group : combinedDTO.getCombined().getGroups()) {
            Swiss toUpdate = swissService.findOne(group.getId());
            
            toUpdate.setColor(combinedDTO.getGroupSettings().getColor());
            toUpdate.setPlayingFields(combinedDTO.getGroupSettings().getPlayingFields().get(group.getName()));
            toUpdate.setPointsForWinning(combinedDTO.getGroupSettings().getPointsForWinning());
            toUpdate.setPointsForLosing(combinedDTO.getGroupSettings().getPointsForLosing());
            toUpdate.setPointsForTie(combinedDTO.getGroupSettings().getPointsForTie());
            toUpdate.setSetsToWin(combinedDTO.getGroupSettings().getSetsToWin());
            toUpdate.setTiesAllowed(combinedDTO.getGroupSettings().getTiesAllowed());
            
            saved.add(swissService.updateSwiss( new SwissDTO(toUpdate, combinedDTO.getSeeding().get(group.getName())) ));
        }
        Combined combined = combinedDTO.getCombined();
        combined.getGroups().clear();
        combined.getGroups().addAll(saved);
        return combined;
    }

    private Combined update_eliminationGroups(CombinedDTO combinedDTO) {
        Set<Elimination> saved = new HashSet<>();
        for (Tournament group : combinedDTO.getCombined().getGroups()) {
            Elimination toUpdate = eliminationService.findOne(group.getId());
            
            toUpdate.setBronzeMatch(combinedDTO.getGroupSettings().getBronzeMatch());
            toUpdate.setType(combinedDTO.getGroupSettings().getEliminationType());
            
            toUpdate.setPointsForWinning(combinedDTO.getGroupSettings().getPointsForWinning());
            toUpdate.setPointsForLosing(combinedDTO.getGroupSettings().getPointsForLosing());
            toUpdate.setPointsForTie(combinedDTO.getGroupSettings().getPointsForTie());
            toUpdate.setSetsToWin(combinedDTO.getGroupSettings().getSetsToWin());
           
            saved.add(eliminationService.updateElimination(toUpdate, combinedDTO.getSeeding().get(group.getName()) ));
        }
        Combined combined = combinedDTO.getCombined();
        combined.getGroups().clear();
        combined.getGroups().addAll(saved);
        return combined;
    }

    private Combined updatePlayoff(CombinedDTO combinedDTO) {
        SetSettings setSettings = combinedDTO.getPlayoffSettings().getSetSettings();
        if(setSettings != null){
            combinedDTO.getPlayoffSettings().setSetSettings(setSettingsService.save(setSettings));
        }
        Combined combined = combinedDTO.getCombined();
        switch (combinedDTO.getCombined().getPlayoffType()){
            case ALL_VERSUS_ALL:
                AllVersusAll allVersusAll = allVersusAllService.findOne(combined.getPlayoff().getId());
                
                allVersusAll.setNumberOfMutualMatches(  combinedDTO.getPlayoffSettings().getNumberOfMutualMatches() );
                allVersusAll.setPlayingFields(          combinedDTO.getPlayoffSettings().getPlayingFields()         );
                allVersusAll.setPointsForWinning(       combinedDTO.getPlayoffSettings().getPointsForWinning()      );
                allVersusAll.setPointsForLosing(        combinedDTO.getPlayoffSettings().getPointsForLosing()       );
                allVersusAll.setPointsForTie(           combinedDTO.getPlayoffSettings().getPointsForTie()          );
                allVersusAll.setSetsToWin(              combinedDTO.getPlayoffSettings().getSetsToWin()             );
                allVersusAll.setTiesAllowed(            combinedDTO.getPlayoffSettings().getTiesAllowed()           );
                
                combined.setPlayoff(allVersusAllService.updateAllVersusAll(allVersusAll));
                return combined;
                
            case SWISS:
                Swiss swiss = swissService.findOne(combined.getPlayoff().getId());
                
                swiss.setColor(combinedDTO.getPlayoffSettings().getColor());
                swiss.setPlayingFields(          combinedDTO.getPlayoffSettings().getPlayingFields()         );
                swiss.setPointsForWinning(       combinedDTO.getPlayoffSettings().getPointsForWinning()      );
                swiss.setPointsForLosing(        combinedDTO.getPlayoffSettings().getPointsForLosing()       );
                swiss.setPointsForTie(           combinedDTO.getPlayoffSettings().getPointsForTie()          );
                swiss.setSetsToWin(              combinedDTO.getPlayoffSettings().getSetsToWin()             );
                swiss.setTiesAllowed(            combinedDTO.getPlayoffSettings().getTiesAllowed()           );
                
                combined.setPlayoff(swissService.updateSwiss(new SwissDTO(swiss)));
                return combined;
            default: //elimination single and double
                Elimination elimination = eliminationService.findOne(combined.getPlayoff().getId());
                elimination.setBronzeMatch(       combinedDTO.getPlayoffSettings().getBronzeMatch());
                elimination.setPointsForWinning(  combinedDTO.getPlayoffSettings().getPointsForWinning()      );
                elimination.setPointsForLosing(   combinedDTO.getPlayoffSettings().getPointsForLosing()       );
                elimination.setPointsForTie(      combinedDTO.getPlayoffSettings().getPointsForTie()          );
                elimination.setSetsToWin(         combinedDTO.getPlayoffSettings().getSetsToWin()             );
                elimination.setType(              combinedDTO.getPlayoffSettings().getEliminationType()       );
                
                combined.setPlayoff(eliminationService.updateElimination(elimination, null));
                return combined;
        }
        
        
    }

    

    
}
