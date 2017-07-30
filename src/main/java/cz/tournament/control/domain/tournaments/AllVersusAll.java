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


    @NotNull
    @Min(value = 1)
    @Column(name = "number_of_mutual_matches", nullable = false)
    private Integer numberOfMutualMatches = 1;
      
    
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
        return Objects.equals(this.getId(), allVersusAll.getId());
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
