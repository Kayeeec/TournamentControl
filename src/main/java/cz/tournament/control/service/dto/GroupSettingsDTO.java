/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service.dto;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.enumeration.EliminationType;
import cz.tournament.control.domain.AllVersusAll;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Karolina Bozkova
 */
public class GroupSettingsDTO {
    private Double pointsForWinning;
    private Double pointsForTie;
    private Double pointsForLosing;
    private Integer setsToWin;
    private SetSettings setSettings;
    
    private Boolean color;                      //S--
    private Integer numberOfMutualMatches;      //-A-
    private Map<String,Integer> playingFields;  //SA-
    private Integer totalPlayingFields;         //SA-
    private Boolean tiesAllowed;                //SA-
    private Boolean bronzeMatch;                //--E
    private EliminationType eliminationType;    //--E
    
    public GroupSettingsDTO() {
    }

    public GroupSettingsDTO(Combined combined) {
        if(!combined.getGroups().isEmpty()){
            Tournament tournament = combined.getGroups().iterator().next();
            this.pointsForWinning = tournament.getPointsForWinning();
            this.pointsForLosing = tournament.getPointsForLosing();
            this.pointsForTie = tournament.getPointsForTie();
            this.setSettings = tournament.getSetSettings();
            this.setsToWin = tournament.getSetsToWin();

            switch (combined.getInGroupTournamentType()) {
                case ALL_VERSUS_ALL:
                    AllVersusAll ava = (AllVersusAll) tournament;
                    this.numberOfMutualMatches = ava.getNumberOfMutualMatches();
                    
                    extract_PlayingFieldsAndTotalPlayingFields(combined.getGroups());
                    this.tiesAllowed = ava.getTiesAllowed();
                    
                    break;
                case SWISS:
                    Swiss swiss = (Swiss) tournament;
                    this.color = swiss.isColor();
                    
                    extract_PlayingFieldsAndTotalPlayingFields(combined.getGroups());
                    this.tiesAllowed = swiss.getTiesAllowed();
                    
                    break;
                default: //elimination single/double
                    Elimination elimination = (Elimination) tournament;
                    this.bronzeMatch = elimination.getBronzeMatch();
                    this.eliminationType = elimination.getType();
            }
        }
    }
    
    public Boolean getBronzeMatch() {
        return bronzeMatch;
    }

    public void setBronzeMatch(Boolean bronzeMatch) {
        this.bronzeMatch = bronzeMatch;
    }

    public Integer getTotalPlayingFields() {
        return totalPlayingFields;
    }

    public void setTotalPlayingFields(Integer totalPlayingFields) {
        this.totalPlayingFields = totalPlayingFields;
    }
    
    

    public Double getPointsForWinning() {
        return pointsForWinning;
    }

    public void setPointsForWinning(Double pointsForWinning) {
        this.pointsForWinning = pointsForWinning;
    }

    public Double getPointsForTie() {
        return pointsForTie;
    }

    public void setPointsForTie(Double pointsForTie) {
        this.pointsForTie = pointsForTie;
    }

    public Double getPointsForLosing() {
        return pointsForLosing;
    }

    public void setPointsForLosing(Double pointsForLosing) {
        this.pointsForLosing = pointsForLosing;
    }

    public Integer getSetsToWin() {
        return setsToWin;
    }

    public void setSetsToWin(Integer setsToWin) {
        this.setsToWin = setsToWin;
    }

    public Boolean getTiesAllowed() {
        return tiesAllowed;
    }

    public void setTiesAllowed(Boolean tiesAllowed) {
        this.tiesAllowed = tiesAllowed;
    }

    public SetSettings getSetSettings() {
        return setSettings;
    }

    public void setSetSettings(SetSettings setSettings) {
        this.setSettings = setSettings;
    }

    public Boolean getColor() {
        return color;
    }

    public void setColor(Boolean color) {
        this.color = color;
    }

    public Integer getNumberOfMutualMatches() {
        return numberOfMutualMatches;
    }

    public void setNumberOfMutualMatches(Integer numberOfMutualMatches) {
        this.numberOfMutualMatches = numberOfMutualMatches;
    }

    public Map<String, Integer> getPlayingFields() {
        return playingFields;
    }

    public void setPlayingFields(Map<String, Integer> playingFields) {
        this.playingFields = playingFields;
    }

    public EliminationType getEliminationType() {
        return eliminationType;
    }

    public void setEliminationType(EliminationType eliminationType) {
        this.eliminationType = eliminationType;
    }
    
    
    
    private String printValueOrNull(Object o){
        if(o != null){
            return o.toString();
        }
        return "null";
    }
    
    

    @Override
    public String toString() {
        return "GroupSettingsDTO{" 
                + "pointsForWinning=" + printValueOrNull(pointsForWinning)
                + ", pointsForTie=" +  printValueOrNull(pointsForTie)
                + ", pointsForLosing=" +  printValueOrNull(pointsForLosing)
                + ", setsToWin=" +  printValueOrNull(setsToWin)
                + ", tiesAllowed=" +  printValueOrNull(tiesAllowed) +System.lineSeparator()
                + ", setSettings=" +  printValueOrNull(setSettings)
                + ", color=" +  printValueOrNull(color)
                + ", numberOfMutualMatches=" +  printValueOrNull(numberOfMutualMatches) +System.lineSeparator()
                + ", playingFields=[" + playingFields_toString() 
                + "] }";
    }
    
    private String playingFields_toString() {
        if(playingFields == null) return "null";
        String str = "";
            for (Map.Entry<String, Integer> playingField : playingFields.entrySet()) {
                String entry = "("+playingField.getKey()+", "+playingField.getValue()+"), ";
            str = str.concat(entry);
            }
            return str;
    }

    private void extract_PlayingFieldsAndTotalPlayingFields(Set<Tournament> groups) {
        int total = 0;
        Map<String,Integer> mapFields = new HashMap<>();
        
        for (Tournament group : groups) {
            mapFields.put(group.getName(), group.getPlayingFields());
            total += group.getPlayingFields();
        }
        
        this.totalPlayingFields = total;
        this.playingFields = mapFields;
    }
    
    
    
    
    
    
}
