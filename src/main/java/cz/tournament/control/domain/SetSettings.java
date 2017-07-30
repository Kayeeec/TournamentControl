package cz.tournament.control.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A SetSettings.
 */
@Entity
@Table(name = "set_settings")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SetSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1)
    @Column(name = "max_score")
    private Integer maxScore;

    @Min(value = 0)
    @Column(name = "min_reached_score")
    private Integer minReachedScore;

    @Min(value = 1)
    @Column(name = "lead_by_points")
    private Integer leadByPoints;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public SetSettings maxScore(Integer maxScore) {
        this.maxScore = maxScore;
        return this;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getMinReachedScore() {
        return minReachedScore;
    }

    public SetSettings minReachedScore(Integer minReachedScore) {
        this.minReachedScore = minReachedScore;
        return this;
    }

    public void setMinReachedScore(Integer minReachedScore) {
        this.minReachedScore = minReachedScore;
    }

    public Integer getLeadByPoints() {
        return leadByPoints;
    }

    public SetSettings leadByPoints(Integer leadByPoints) {
        this.leadByPoints = leadByPoints;
        return this;
    }

    public void setLeadByPoints(Integer leadByPoints) {
        this.leadByPoints = leadByPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetSettings setSettings = (SetSettings) o;
        if (setSettings.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, setSettings.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SetSettings{" +
            "id=" + id +
            ", maxScore='" + maxScore + "'" +
            ", minReachedScore='" + minReachedScore + "'" +
            ", leadByPoints='" + leadByPoints + "'" +
            '}';
    }
}
