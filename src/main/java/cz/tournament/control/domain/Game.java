package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

/**
 * A Game.
 */
@Entity
@Table(name = "game")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "finished")
    private Boolean finished = false;

    
    @Column(name = "round")
    private Integer round;

    @Min(value = 1)
    @Column(name = "period")
    private Integer period;

    @Column(name = "note")
    private String note;

    @Column(name = "playing_field")
    private Integer playingField;


    @JsonIgnoreProperties({"matches"})
    @ManyToOne()
    private Tournament tournament;

    @ManyToOne
    private Participant rivalA;

    @ManyToOne
    private Participant rivalB;

    @OneToMany(mappedBy = "game",  fetch = FetchType.EAGER, 
            cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnoreProperties({"game"})
    private Set<GameSet> sets = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isFinished() {
        return finished;
    }

    public Game finished(Boolean finished) {
        this.finished = finished;
        return this;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Integer getRound() {
        return round;
    }

    public Game round(Integer round) {
        this.round = round;
        return this;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Integer getPeriod() {
        return period;
    }

    public Game period(Integer period) {
        this.period = period;
        return this;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getNote() {
        return note;
    }

    public Game note(String note) {
        this.note = note;
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPlayingField() {
        return playingField;
    }

    public Game playingField(Integer playingField) {
        this.playingField = playingField;
        return this;
    }

    public void setPlayingField(Integer playingField) {
        this.playingField = playingField;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public Game tournament(Tournament tournament) {
        this.tournament = tournament;
        return this;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Participant getRivalA() {
        return rivalA;
    }

    public Game rivalA(Participant participant) {
        this.rivalA = participant;
        return this;
    }

    public void setRivalA(Participant participant) {
        this.rivalA = participant;
    }

    public Participant getRivalB() {
        return rivalB;
    }

    public Game rivalB(Participant participant) {
        this.rivalB = participant;
        return this;
    }

    public void setRivalB(Participant participant) {
        this.rivalB = participant;
    }

    public Set<GameSet> getSets() {
        return sets;
    }

    public Game sets(Set<GameSet> gameSets) {
        this.sets = gameSets;
        return this;
    }

    public Game addSets(GameSet gameSet) {
        this.sets.add(gameSet);
        gameSet.setGame(this);
        return this;
    }

    public Game removeSets(GameSet gameSet) {
        this.sets.remove(gameSet);
        gameSet.setGame(null);
        return this;
    }

    public void setSets(Set<GameSet> gameSets) {
        this.sets = gameSets;
    }

    public Boolean allSetsFinished(){
        for (GameSet set : sets) {
            if(!set.isFinished()) return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        if (game.id == null || id == null) {
            return false;
        }
//        if(this.id != null && game.id != null){
//            return Objects.equals(this.id, game.getId());
//        }
        return Objects.equals(this.finished, game.isFinished())
                && Objects.equals(this.note, game.getNote())
                && Objects.equals(this.period, game.getPeriod())
                && Objects.equals(this.playingField, game.getPlayingField())
                && Objects.equals(this.rivalA, game.getRivalA())
                && Objects.equals(this.rivalB, game.getRivalB())
                && Objects.equals(this.round, game.getRound())
                && Objects.equals(this.sets, game.getSets())
                && Objects.equals(this.tournament, game.getTournament())
                && Objects.equals(this.id, game.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + Objects.hashCode(id);
        result = prime * result + Objects.hashCode(finished);
        result = prime * result + Objects.hashCode(note);
        result = prime * result + Objects.hashCode(period);
        result = prime * result + Objects.hashCode(playingField);
        result = prime * result + Objects.hashCode(rivalA);
        result = prime * result + Objects.hashCode(rivalB);
        result = prime * result + Objects.hashCode(round);
        result = prime * result + Objects.hashCode(tournament);
        return result;
    }

    

    @Override
    public String toString() {
        String base = "Game{" +
            "id=" + this.getId() +
            ", finished='" + this.isFinished() + "'" +
            ", round='" + this.getRound() + "'" +
            ", period='" + this.getPeriod() + "'";
        String concatA;
        if(this.rivalA != null){
            concatA = base.concat(", rivalA: " + rivalA.toString());
        }else{
            concatA = base.concat(", rivalA: 'null'");
        }
        String concatB;
        if(this.rivalB != null){
            concatB = concatA.concat(", rivalB: " + rivalB.toString());
        }else{
            concatB = concatA.concat(", rivalB: 'null'");
        }
        String result = concatB.concat("}");
        return result;
    }
    
    public String setsToString(){
        List<Long> setIDs = new ArrayList<>();
        for (GameSet set : this.getSets()) {
            setIDs.add(set.getId());
}
        return setIDs.toString();
    }
    
    public static Comparator<Game> PeriodRoundComparator
            = new Comparator<Game>() {
        @Override
        public int compare(Game game1, Game game2) {
            
            if (game1 == null || game2 == null) {
                throw new NullPointerException("Game.compareTo(Game o) : o is null");
            }

            Integer period1 = game1.getPeriod();
            Integer period2 = game2.getPeriod();

            //ascending order
            int byPeriod = period1.compareTo(period2);
            if(byPeriod != 0){
                return byPeriod;
            }
            return game1.getRound().compareTo(game2.getRound());
        }

    };
    
    public static Comparator<Game> RoundComparator
            = new Comparator<Game>() {
        @Override
        public int compare(Game game1, Game game2) {
            
            if (game1 == null || game2 == null) {
                throw new NullPointerException("Game.compareTo(Game o) : o is null");
            }
            return game1.getRound().compareTo(game2.getRound());
        }

    };
    
    @JsonIgnore
    public boolean allSetsFinished_And_NotATie(){
        int wonSetsA = 0;
        int wonSetsB = 0;
        
        for (GameSet set : sets) {
            if(!set.isFinished()) return false;
            if(set.getScoreA() > set.getScoreB()) wonSetsA += 1;
            if(set.getScoreA() < set.getScoreB()) wonSetsB += 1;
        }
        Integer setsToWin = this.getTournament().getSetsToWin();
        if (setsToWin != null) {
            return wonSetsA >= setsToWin || wonSetsB >= setsToWin;
        } else {
            return wonSetsA != wonSetsB;
        }
    }

    /**
     * Sums up all scores of rivalA from sets.
     *
     * @return int, sum of all rivalA scores from all sets
     */
    @JsonIgnore
    public int getSumOfScoresA() {
        int result = 0;
        for (GameSet set : sets) {
           Integer a = set.getScoreA();
           if(a != null){
               result += a;
           }
        }
        return result;
    }

    /**
     * Sums up all scores of rivalB from sets.
     *
     * @return int, sum of all rivalB scores from all sets
     */
    @JsonIgnore
    public int getSumOfScoresB() {
        int result = 0;
        if (!this.getSets().isEmpty()) {
            for (GameSet set : this.getSets()) {
                Integer b = set.getScoreB();
                if (b != null) {
                    result += b;
                }
            }
        }
        return result;
    }
    
    /**
     * Sums up all scores of rivalA and rivalB from sets.
     *
     * @return Map<String,Integer>, keys are "A" and "B"
     */
    @JsonIgnore
    public Map<String,Integer> getSumsOfScores() {
        Map<String,Integer> result = new HashMap();
        result.put("A", 0 );
        result.put("B", 0 );
        
        if (!this.getSets().isEmpty()) {
            for (GameSet set : this.getSets()) {
                Integer a = set.getScoreA();
                Integer b = set.getScoreB();
                if(a != null){
                    int tmpA = result.get("A") + a;
                    result.put("A", tmpA);
                }
                if (b != null) {
                    int tmpB = result.get("B") + b;
                    result.put("B", tmpB);
                }
            }
        }
        return result;
    }
    
    
    
    /**
     *  Determines winner of the game. 
     * @return Participant if all sets are finished and it is not a tie, or one of rivals is BYE
     *         null if there is an unfinished set, game is a tie or one or both rivals are null.
     */
    @JsonAnyGetter
    public Map<String,Participant> getWinnerAndLoser(){
        Map<String,Participant> result = new HashMap();
        result.put("winner", null);
        result.put("loser", null);
        if(!finished){
            return result;
        }
        if(rivalA == null || rivalB == null){
            return result;
        }
        if(rivalA.isBye()){
            if(rivalB.isBye()){
                result.put("winner", rivalA);
                result.put("loser", rivalB);
                return result;
            }
            //B not BYE and not null
            result.put("winner", rivalB);
            result.put("loser", rivalA);
            return result;
        }
        if(rivalB.isBye()){
            result.put("winner", rivalA);
            result.put("loser", rivalB);
            return result;
        }
        
        int wonSetsA = 0;
        int wonSetsB = 0;
        
        if (!this.getSets().isEmpty()) {
            for (GameSet set : this.getSets()) {
                if(!set.isFinished()){
                    return result;
                }
                if(set.getScoreA() > set.getScoreB()){
                    wonSetsA += 1;
                }
                if(set.getScoreA() < set.getScoreB()){
                    wonSetsB += 1;
                }
                //else is tie
            }
        }
        Integer setsToWin = this.getTournament().getSetsToWin();
        if (setsToWin != null) {
            if(wonSetsA >= setsToWin){
                result.put("winner", rivalA);
                result.put("loser", rivalB);
                return result;
            }
            if(wonSetsB >= setsToWin){
                result.put("winner", rivalB);
                result.put("loser", rivalA);
                return result;
            }
            return result;
        } else {
            if(wonSetsA > wonSetsB){
                result.put("winner", rivalA);
                result.put("loser", rivalB);
                return result;
            }
            if(wonSetsB > wonSetsA){
                result.put("winner", rivalB);
                result.put("loser", rivalA);
                return result;
            }
            //tie
            return result;
        }
    }
}
