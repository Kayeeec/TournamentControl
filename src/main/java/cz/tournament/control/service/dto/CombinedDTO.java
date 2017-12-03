package cz.tournament.control.service.dto;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Participant;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Karolina Bozkova
 */
public class CombinedDTO {
    private Combined combined;
    private GroupSettingsDTO groupSettings;
    private PlayoffSettingsDTO playoffSettings;
    
    private Map<String,List<Participant>> grouping; //null if automatically
    private Map<String,List<Participant>> seeding; //null if automatically

    public CombinedDTO() {
    }

    public Combined getCombined() {
        return combined;
    }

    public void setCombined(Combined combined) {
        this.combined = combined;
    }

    public GroupSettingsDTO getGroupSettings() {
        return groupSettings;
    }

    public void setGroupSettings(GroupSettingsDTO groupSettings) {
        this.groupSettings = groupSettings;
    }

    public PlayoffSettingsDTO getPlayoffSettings() {
        return playoffSettings;
    }

    public void setPlayoffSettings(PlayoffSettingsDTO playoffSettings) {
        this.playoffSettings = playoffSettings;
    }

    public Map<String, List<Participant>> getGrouping() {
        return grouping;
    }

    public void setGrouping(Map<String, List<Participant>> grouping) {
        this.grouping = grouping;
    }

    public Map<String, List<Participant>> getSeeding() {
        return seeding;
    }

    public void setSeeding(Map<String, List<Participant>> seeding) {
        this.seeding = seeding;
    }
    
     

    @Override
    public String toString() {
        return "CombinedDTO{" + "combined=" +  printValueOrNull(combined) +System.lineSeparator()
                + ", groupSettingsDTO=" +  printValueOrNull(groupSettings) +System.lineSeparator()
                + ", playoffSettingsDTO=" +  printValueOrNull(playoffSettings) +System.lineSeparator()
                + ", grouping=" + mapStringParticipantList_toString(grouping) +System.lineSeparator()
                + ", seeding=" + mapStringParticipantList_toString(seeding) +System.lineSeparator()
                + '}';
    }
    
    private String printValueOrNull(Object o){
        if(o != null){
            return o.toString();
        }
        return "null";
    }

    private String mapStringParticipantList_toString(Map<String, List<Participant>> map) {
        String str = "[";
        for (Map.Entry<String, List<Participant>> entry : map.entrySet()) {
            String group = entry.getKey();
            List<Participant> participantList = entry.getValue();
            
            str = str.concat("("+group+", "+participantList_toString(participantList)+"), "+System.lineSeparator());
            
        }
        str = str.concat(" ]");
        return str;
    }

    private String participantList_toString(List<Participant> participantList) {
        String str = "[";
        for (Participant participant : participantList) {
            str = str.concat(participant.getName()+", ");
        }
        str = str.concat(" ]");
        return str;
    }
    
    

    
    
    
    
    
    
}
