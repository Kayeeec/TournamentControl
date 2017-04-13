package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private Tournament tournament;

    @ManyToOne
    private Participant rivalA;

    @ManyToOne
    private Participant rivalB;

    @OneToMany(mappedBy = "game")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
        return "Game{"
                + "id=" + id
                + ", finished='" + finished + "'"
                + ", round='" + round + "'"
                + ", period='" + period + "'"
                + ", note='" + note + "'"
                + '}';
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
     * @return int, sum of all rivalA scores from all sets
     */
    public int getAllScoresA(){
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
     * @return int, sum of all rivalB scores from all sets
     */
    public int getAllScoresB(){
        int result = 0;
        for (GameSet set : sets) {
           Integer b = set.getScoreB();
           if(b != null){
               result += b;
           }
        }
        return result;
    }
}
