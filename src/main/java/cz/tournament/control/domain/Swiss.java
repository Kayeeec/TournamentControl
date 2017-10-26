package cz.tournament.control.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Swiss.
 */
@Entity
@Table(name = "swiss")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@PrimaryKeyJoinColumn(name="id")
public class Swiss extends Tournament implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "rounds")
    private Integer rounds;

    @Column(name = "rounds_to_generate")
    private Integer roundsToGenerate;

    @Column(name = "color")
    private Boolean color;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    @Override
    public String getTournamentType(){
        return "swiss";
    }
    
    public Integer getRounds() {
        return rounds;
    }

    public Swiss rounds(Integer rounds) {
        this.rounds = rounds;
        return this;
    }

    public void setRounds(Integer rounds) {
        this.rounds = rounds;
    }

    public Integer getRoundsToGenerate() {
        return roundsToGenerate;
    }

    public Swiss roundsToGenerate(Integer roundsToGenerate) {
        this.roundsToGenerate = roundsToGenerate;
        return this;
    }

    public void setRoundsToGenerate(Integer roundsToGenerate) {
        this.roundsToGenerate = roundsToGenerate;
    }

    public Boolean isColor() {
        return color;
    }

    public Swiss color(Boolean color) {
        this.color = color;
        return this;
    }

    public void setColor(Boolean color) {
        this.color = color;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Swiss swiss = (Swiss) o;
        if (swiss.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), swiss.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Swiss{" +
            "id=" + getId() +
            ", rounds='" + getRounds() + "'" +
            ", roundsToGenerate='" + getRoundsToGenerate() + "'" +
            ", color='" + isColor() + "'" +
            "}";
    }
}
