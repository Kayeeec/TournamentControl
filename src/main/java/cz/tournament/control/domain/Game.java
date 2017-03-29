package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
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

    @Min(value = 0)
    @Column(name = "score_a")
    private Integer scoreA = 0;

    @Min(value = 0)
    @Column(name = "score_b")
    private Integer scoreB = 0;

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

    
    
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getScoreA() {
        return scoreA;
    }

    public Game scoreA(Integer scoreA) {
        this.scoreA = scoreA;
        return this;
    }

    public void setScoreA(Integer scoreA) {
        this.scoreA = scoreA;
    }

    public Integer getScoreB() {
        return scoreB;
    }

    public Game scoreB(Integer scoreB) {
        this.scoreB = scoreB;
        return this;
    }

    public void setScoreB(Integer scoreB) {
        this.scoreB = scoreB;
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
            "id=" + id +
            ", scoreA='" + scoreA + "'" +
            ", scoreB='" + scoreB + "'" +
            ", finished='" + finished + "'" +
            ", round='" + round + "'" +
            ", period='" + period + "'" +
            ", note='" + note + "'" +
            '}';
    }
}
