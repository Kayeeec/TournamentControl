package cz.tournament.control.domain.tournaments;

import cz.tournament.control.domain.Tournament;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A AllVersusAll.
 */
@Entity
@Table(name = "all_versus_all")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@PrimaryKeyJoinColumn(name="id")
public class AllVersusAll extends Tournament implements Serializable  {

    private static final long serialVersionUID = 1L;
    public static final String TOURNAMENT_TYPE = "allVersusAll";


    @NotNull
    @Min(value = 1)
    @Column(name = "number_of_mutual_matches", nullable = false)
    private Integer numberOfMutualMatches = 1;

    public AllVersusAll() {
        super();
    }
    
    public Integer getNumberOfMutualMatches() {
        return numberOfMutualMatches;
    }

    public AllVersusAll numberOfMutualMatches(Integer numberOfMutualMatches) {
        this.numberOfMutualMatches = numberOfMutualMatches;
        return this;
    }

    public void setNumberOfMutualMatches(Integer numberOfMutualMatches) {
        this.numberOfMutualMatches = numberOfMutualMatches;
    }
    
    @Override
    public String getTournamentType(){
        return TOURNAMENT_TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AllVersusAll allVersusAll = (AllVersusAll) o;
        if (allVersusAll.getId() == null || this.getId() == null) {
            return false;
        }
//        if(this.getId()!= null && allVersusAll.getId()!= null){
//            return Objects.equals(this.getId(), allVersusAll.getId());
//        }
        return Objects.equals(this.getCreated(), allVersusAll.getCreated())
                && Objects.equals(this.getMatches(), allVersusAll.getMatches())
                && Objects.equals(this.getName(), allVersusAll.getName())
                && Objects.equals(this.getNote(), allVersusAll.getNote())
                && Objects.equals(this.getParticipants(), allVersusAll.getParticipants())
                && Objects.equals(this.getPlayingFields(), allVersusAll.getPlayingFields())
                && Objects.equals(this.getPointsForLosing(), allVersusAll.getPointsForLosing())
                && Objects.equals(this.getPointsForTie(), allVersusAll.getPointsForTie())
                && Objects.equals(this.getPointsForWinning(), allVersusAll.getPointsForWinning())
                && Objects.equals( this.getSetSettings(), allVersusAll.getSetSettings())
                && Objects.equals(this.getSetsToWin(), allVersusAll.getSetsToWin())
                && Objects.equals(this.getTiesAllowed(), allVersusAll.getTiesAllowed())
                && Objects.equals(this.getUser(), allVersusAll.getUser())
                
                && Objects.equals(this.getNumberOfMutualMatches(), allVersusAll.getNumberOfMutualMatches());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId());
    }

    @Override
    public String toString() {
        return "AllVersusAll{" +
            "id=" + this.getId() +
            ", name='" + this.getName() + "'" +
            ", note='" + this.getNote()+ "'" +
            ", pointsForWinning='" + this.getPointsForWinning()+ "'" +
            ", pointsForLosing='" + this.getPointsForLosing()+ "'" +
            ", pointsForTie='" + this.getPointsForTie()+ "'" +
            ", created='" + this.getCreated()+ "'" +
            ", setsToWin='" + this.getSetsToWin()+ "'" +
            ", tiesAllowed='" + this.getTiesAllowed()+ "'" +
            ", playingFields='" + this.getPlayingFields()+ "'" +
            ", numberOfMutualMatches='" + numberOfMutualMatches + "'" +
            '}';
    }
    
}
