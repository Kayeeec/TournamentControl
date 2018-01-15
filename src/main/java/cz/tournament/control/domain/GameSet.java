package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

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

    @ManyToOne
    @JsonIgnoreProperties({"sets"})
    private Game game;

    @ManyToOne
    @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    private SetSettings setSettings;
    
//    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
//    private SetSettings setSettings;

    public GameSet() {
    }
    
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

    public SetSettings getSetSettings() {
        return setSettings;
    }

    public GameSet setSettings(SetSettings setSettings) {
        this.setSettings = setSettings;
        return this;
    }

    public void setSetSettings(SetSettings setSettings) {
        this.setSettings = setSettings;
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
//        if(this.id !=null && gameSet.getId() != null){
//            return Objects.equals(id, gameSet.getId());
//        }
        return Objects.equals(finished, gameSet.isFinished())
                && Objects.equals(game, gameSet.getGame())
                && Objects.equals(scoreA, gameSet.getScoreA())
                && Objects.equals(scoreB, gameSet.getScoreB())
                && Objects.equals(setSettings, gameSet.getSetSettings())
                && Objects.equals(id, gameSet.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 1;
        result = prime * result + Objects.hashCode(id);
        result = prime * result + Objects.hashCode(finished);
        result = prime * result + Objects.hashCode(game);
        result = prime * result + Objects.hashCode(scoreA);
        result = prime * result + Objects.hashCode(scoreB);
        result = prime * result + Objects.hashCode(setSettings);
        return result;
    }

    @Override
    public String toString() {
        return "GameSet{" +
            "id=" + id +
            ", scoreA='" + scoreA + "'" +
            ", scoreB='" + scoreB + "'" +
            ", finished='" + finished + "'" +
            '}';
    }
}
