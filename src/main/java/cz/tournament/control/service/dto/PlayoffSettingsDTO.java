package cz.tournament.control.service.dto;

import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.SetSettings;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.tournaments.AllVersusAll;

/**
 *
 * @author Karolina Bozkova
 */
public class PlayoffSettingsDTO {
    private Double pointsForWinning;
    private Double pointsForTie;
    private Double pointsForLosing;
    private Integer setsToWin;
    private SetSettings setSettings;
    
    private Boolean color;                  //S--
    private Integer numberOfMutualMatches;  //-A-
    private Integer playingFields;          //SA-
    private Boolean tiesAllowed;            //SA-
    private Boolean  bronzeMatch;           //--E
    
    

    public PlayoffSettingsDTO() {
    }

    PlayoffSettingsDTO(Combined combined) {
        if(combined.getPlayoff() != null){
            Tournament tournament = combined.getPlayoff();
            this.pointsForWinning = tournament.getPointsForWinning();
            this.pointsForLosing = tournament.getPointsForLosing();
            this.pointsForTie = tournament.getPointsForTie();
            this.setSettings = tournament.getSetSettings();
            this.setsToWin = tournament.getSetsToWin();
            
            switch (combined.getPlayoffType()) {
                case ALL_VERSUS_ALL:
                    AllVersusAll allVersusAll = (AllVersusAll) tournament;
                    this.numberOfMutualMatches = allVersusAll.getNumberOfMutualMatches();
                    
                    this.playingFields = allVersusAll.getPlayingFields();
                    this.tiesAllowed = allVersusAll.getTiesAllowed();
                    
                    break;
                case SWISS:
                    Swiss swiss = (Swiss) tournament;
                    this.color = swiss.isColor();
                    
                    this.playingFields = swiss.getPlayingFields();
                    this.tiesAllowed = swiss.getTiesAllowed();
                    
                    break;
                default: //elimination single/double
                    Elimination elimination = (Elimination) tournament;
                    this.bronzeMatch = elimination.getBronzeMatch();
                    
            }
            
        }
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
