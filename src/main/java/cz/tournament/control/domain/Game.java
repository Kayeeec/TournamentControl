package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

/**
 * A Game.
 */
@Entity
@Table(name = "game")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Game implements Serializable, Comparable<Game> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "finished")
    private Boolean finished = false;

    @Min(value = 1)
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

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
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
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Game{" +
            "id=" + this.getId() +
            ", finished='" + this.isFinished() + "'" +
            ", round='" + this.getRound() + "'" +
            ", period='" + this.getPeriod() + "'" +
            '}';
    }
    
    public String setsToString(){
        List<Long> setIDs = new ArrayList<>();
        for (GameSet set : this.getSets()) {
            setIDs.add(set.getId());
}
        return setIDs.toString();
    }

    @Override
    public int compareTo(Game o) {
        if (o == null) {
            throw new NullPointerException("Game.compareTo(Game o) : o is null");
        }

        int byPeriod = this.period.compareTo(o.period);
        if (byPeriod != 0) {
            return byPeriod;
        }

        return this.round.compareTo(o.round);

    }

    /**
     * Sums up all scores of rivalA from sets.
     *
     * @return int, sum of all rivalA scores from all sets
     */
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
     *  Determines winner of the game. 
     * @return Participant if all sets are finished and it is not a tie, 
     * null if there is an unfinished set, game is a tie or both rivals are null.
     */
    public Participant getWinner(){
        if(rivalA != null && rivalB == null) return rivalA;
        if(rivalB != null && rivalA == null) return rivalB;
        if(rivalA == null && rivalB == null) return null;
        
        int wonSetsA = 0;
        int wonSetsB = 0;
        
        if (!this.getSets().isEmpty()) {
            for (GameSet set : this.getSets()) {
                if(set.isFinished()==false){
                    return null;
                }
                if(set.getScoreA() > set.getScoreB()){
                    wonSetsA += 1;
                }
                if(set.getScoreA() < set.getScoreB()){
                    wonSetsB += 1;
                }
            }
        }
        Integer setsToWin = this.getTournament().getSetsToWin();
        if (setsToWin != null) {
            if(wonSetsA == setsToWin) return this.rivalA;
            if(wonSetsB == setsToWin) return this.rivalB;
            return null;
        } else {
            if(wonSetsA > wonSetsB) return this.rivalA;
            if(wonSetsB > wonSetsA) return this.rivalB;
            return null;
        }
    }
}
