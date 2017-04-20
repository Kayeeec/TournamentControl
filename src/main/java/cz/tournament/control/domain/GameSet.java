package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A GameSet.
 */
@Entity
@Table(name = "game_set")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GameSet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score_a")
    private Integer scoreA = 0;

    @Column(name = "score_b")
    private Integer scoreB = 0;

    @Column(name = "finished")
    private Boolean finished = false;
    
    @JsonIgnore
    @ManyToOne
    private Game game;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getScoreA() {
        return scoreA;
    }

    public GameSet scoreA(Integer scoreA) {
        this.scoreA = scoreA;
        return this;
    }

    public void setScoreA(Integer scoreA) {
        this.scoreA = scoreA;
    }

    public Integer getScoreB() {
        return scoreB;
    }

    public GameSet scoreB(Integer scoreB) {
        this.scoreB = scoreB;
        return this;
    }

    public void setScoreB(Integer scoreB) {
        this.scoreB = scoreB;
    }

    public Boolean isFinished() {
        return finished;
    }

    public GameSet finished(Boolean finished) {
        this.finished = finished;
        return this;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Game getGame() {
        return game;
    }

    public GameSet game(Game game) {
        this.game = game;
        return this;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameSet gameSet = (GameSet) o;
        if (gameSet.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, gameSet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "GameSet{" +
            "id=" + id +
            ", game ='" + game.toString()+ "'" +
            '}';
    }
}
