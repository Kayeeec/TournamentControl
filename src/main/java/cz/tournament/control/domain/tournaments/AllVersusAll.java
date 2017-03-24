package cz.tournament.control.domain.tournaments;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Tournament;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @NotNull
    @Min(value = 1)
    @Column(name = "number_of_mutual_matches", nullable = false)
    private Integer numberOfMutualMatches;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

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
            ", numberOfMutualMatches='" + numberOfMutualMatches + "'" +
            '}';
    }
    
    private int getNumberOfRounds() {
        //při sudém počtu týmů je počet kol N–1, při lichém počtu je počet kol roven počtu týmů N
       int n = this.getParticipants().size();
       if (n % 2 == 0) return n-1;
       return n;
    }
    
    private Integer[] assignmentInit(int n){
        if(n%2==1){
            Integer[] result = new Integer[n + 1];
            for (int i = 0; i < n; i++){
                result[i]=i;
            }
            result[n] = null;
            return result;
        }
        Integer[] result = new Integer[n];
        for (int i = 0; i < n; i++){
            result[i]=i;
        }
        return result;
    }
    
    private void generateMatches(List<Participant> arr, Integer[] index, int round, int period){
        int n = index.length;
        for (int i = 0; i <= n/2; i++){
            if (index[i] != null && index[n-1-i] != null){
                Game match = new Game(period, round, arr.get(index[i]), arr.get(index[n-1-i]), this);
                this.addMatches(match);  
            }  
        }
    }
    
    private Integer[] shiftIndices(Integer[] index){
        int n = index.length;
        Integer[] result = new Integer[n];
        
        for (int i = 1; i <= n-2; i++){
            result[i+1]=index[i];
        }
        result[1]=index[n-1];
        
        return result;
    }
    
    public void generateAssignment(){
        int numberOfPeriods = getNumberOfMutualMatches();
        
        List<Participant> arr = new ArrayList<>(this.getParticipants());
        Integer[] index = assignmentInit(arr.size());
        
        for (int period = 1; period <= numberOfPeriods; period++) {
            for (int round = 1; round <= getNumberOfRounds(); round++){
                generateMatches(arr, index, round, period);
                index = shiftIndices(index);
            }
        }  
    }
}
