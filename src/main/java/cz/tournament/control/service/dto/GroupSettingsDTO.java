/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service.dto;

import cz.tournament.control.domain.SetSettings;
import java.util.Map;

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
    
    private Boolean color;                      //swiss
    private Integer numberOfMutualMatches;      //          allVersusAll
    private Map<String,Integer> playingFields;  //swiss,    allVersusAll
    private Integer totalPlayingFields;         //swiss,    allVersusAll
    private Boolean tiesAllowed;                //swiss,    allVersusAll
    
    private Boolean bronzeMatch; //elimination_single, elimination_double
    
    public GroupSettingsDTO() {
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
        String str = "";
            for (Map.Entry<String, Integer> playingField : playingFields.entrySet()) {
                String entry = "("+playingField.getKey()+", "+playingField.getValue()+"), ";
            str = str.concat(entry);
            }
            return str;
    }
    
    
    
    
    
    
}
