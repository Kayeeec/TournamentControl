package cz.tournament.control.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

import cz.tournament.control.domain.enumeration.EliminationType;

/**
 * A Elimination.
 */
@Entity
@Table(name = "elimination")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@PrimaryKeyJoinColumn(name="id")
public class Elimination extends Tournament implements Serializable {

    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private EliminationType type = EliminationType.SINGLE;

    @Column(name = "bronze_match")
    private Boolean bronzeMatch = true;


    public EliminationType getType() {
        return type;
    }

    public Elimination type(EliminationType type) {
        this.type = type;
        return this;
    }

    public void setType(EliminationType type) {
        this.type = type;
    }

    public Boolean getBronzeMatch() {
        return bronzeMatch;
    }

    public Elimination bronzeMatch(Boolean bronzeMatch) {
        this.bronzeMatch = bronzeMatch;
        return this;
    }

    public void setBronzeMatch(Boolean bronzeMatch) {
        this.bronzeMatch = bronzeMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Elimination elimination = (Elimination) o;
        if (elimination.getId() == null || this.getId() == null) {
            return false;
        }
        return Objects.equals(this.getId(), elimination.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId());
    }

    @Override
    public String toString() {
        return "Elimination{" +
            "id=" + this.getId() +
            "name=" + this.getName()+
            "type=" + this.getType()+
            '}';
    }
}
