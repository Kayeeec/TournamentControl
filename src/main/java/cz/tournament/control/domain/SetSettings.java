package cz.tournament.control.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
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
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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
//        if(this.id != null && setSettings.getId()!=null){
//            return Objects.equals(this.id, setSettings.getId()); 
//        }
        return Objects.equals(this.leadByPoints, setSettings.getLeadByPoints())
                && Objects.equals(this.maxScore, setSettings.getMaxScore())
                && Objects.equals(this.minReachedScore, setSettings.getMinReachedScore())
                && Objects.equals(this.id, setSettings.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(id);
        result = prime * result + Objects.hashCode(leadByPoints);
        result = prime * result + Objects.hashCode(maxScore);
        result = prime * result + Objects.hashCode(minReachedScore);
        return result;
    }

    @Override
    public String toString() {
        return "SetSettings{" +
            "id=" + getId() +
            ", maxScore='" + getMaxScore() + "'" +
            ", minReachedScore='" + getMinReachedScore() + "'" +
            ", leadByPoints='" + getLeadByPoints() + "'" +
            "}";
    }
}
