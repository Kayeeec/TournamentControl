package cz.tournament.control.service.dto;

import cz.tournament.control.domain.SetSettings;

/**
 *
 * @author Karolina Bozkova
 */
public class PlayoffSettingsDTO {
    private Double pointsForWinning;
    private Double pointsForTie;
    private Double pointsForLosing;
    
    private Integer setsToWin;
    private Boolean tiesAllowed;
    
    private SetSettings setSettings;
    
    private Boolean color;
    private Integer numberOfMutualMatches;
    private Boolean  bronzeMatch;
    
    private Integer playingFields;

    public PlayoffSettingsDTO() {
    }

    public Boolean getBronzeMatch() {
        return bronzeMatch;
    }

    public void setBronzeMatch(Boolean bronzeMatch) {
        this.bronzeMatch = bronzeMatch;
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

    public Integer getPlayingFields() {
        return playingFields;
    }

    public void setPlayingFields(Integer playingFields) {
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
        return "PlayoffSettingsDTO{" 
                + "pointsForWinning=" +  printValueOrNull(pointsForWinning)
                + ", pointsForTie=" +  printValueOrNull(pointsForTie)
                + ", pointsForLosing=" +  printValueOrNull(pointsForLosing)
                + ", setsToWin=" +  printValueOrNull(setsToWin)
                + ", tiesAllowed=" +  printValueOrNull(tiesAllowed) +System.lineSeparator()
                + ", setSettings=" +  printValueOrNull(setSettings)
                + ", color=" +  printValueOrNull(color)
                + ", numberOfMutualMatches=" +  printValueOrNull(numberOfMutualMatches)
                + ", playingFields=" +  printValueOrNull(playingFields)
                        + '}';
    }
    
    
    
}
