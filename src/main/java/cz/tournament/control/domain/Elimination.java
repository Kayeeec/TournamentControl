package cz.tournament.control.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import cz.tournament.control.domain.enumeration.EliminationType;

/**
 * A Elimination.
 */
@Entity
@Table(name = "elimination")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@PrimaryKeyJoinColumn(name="id")
public class Elimination extends Tournament implements Serializable {

    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private EliminationType type = EliminationType.SINGLE;

    @Column(name = "bronze_match")
    private Boolean bronzeMatch = true;


    public EliminationType getType() {
        return type;
    }

    public Elimination type(EliminationType type) {
        this.type = type;
        return this;
    }

    public void setType(EliminationType type) {
        this.type = type;
    }

    public Boolean getBronzeMatch() {
        return bronzeMatch;
    }

    public Elimination bronzeMatch(Boolean bronzeMatch) {
        this.bronzeMatch = bronzeMatch;
        return this;
    }

    public void setBronzeMatch(Boolean bronzeMatch) {
        this.bronzeMatch = bronzeMatch;
    }
    
    @Override
    public String getTournamentType(){
        return "elimination";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Elimination elimination = (Elimination) o;
        if (elimination.getId() == null || this.getId() == null) {
            return false;
        }
//        if(this.getId() != null && elimination.getId()!= null){
//            return Objects.equals(this.getId(), elimination.getId());
//        }
        return Objects.equals(this.getId(), elimination.getId()) 
                && Objects.equals(this.getCreated(), elimination.getCreated())
                && Objects.equals(this.getMatches(), elimination.getMatches())
                && Objects.equals(this.getName(), elimination.getName())
                && Objects.equals(this.getNote(), elimination.getNote())
                && Objects.equals(this.getParticipants(), elimination.getParticipants())
                && Objects.equals(this.getPlayingFields(), elimination.getPlayingFields())
                && Objects.equals(this.getPointsForLosing(), elimination.getPointsForLosing())
                && Objects.equals(this.getPointsForTie(), elimination.getPointsForTie())
                && Objects.equals(this.getPointsForWinning(), elimination.getPointsForWinning())
                && Objects.equals( this.getSetSettings(), elimination.getSetSettings())
                && Objects.equals(this.getSetsToWin(), elimination.getSetsToWin())
                && Objects.equals(this.getTiesAllowed(), elimination.getTiesAllowed())
                && Objects.equals(this.getUser(), elimination.getUser())
                
                && Objects.equals(this.getBronzeMatch(), elimination.getBronzeMatch())
                && Objects.equals(this.getType(), elimination.getType()); 
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId());
    }

    @Override
    public String toString() {
        return "Elimination{" +
            "id=" + this.getId() +
            "name=" + this.getName()+
            "type=" + this.getType()+
            '}';
    }
    
    /**
     * Algorithm from wiki: https://en.wikipedia.org/wiki/Power_of_two#Fast_algorithm_to_check_if_a_positive_number_is_a_power_of_two
     * @param n int, positive, number of participants
     * @return nearest power of two bigger or equal to n
     */
    private int getNextPowerOfTwo(int n){
        if ((n & (n - 1)) == 0) {
            return n;
        }

        while ((n & (n - 1)) != 0) {
            n = n & (n - 1);
        }

        n = n << 1;
        return n;
    }
    
    
    public int getN(){
        return getNextPowerOfTwo(this.getParticipants().size());
    }
}
