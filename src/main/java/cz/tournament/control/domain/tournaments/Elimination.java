package cz.tournament.control.domain.tournaments;

import cz.tournament.control.domain.Tournament;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Elimination.
 */
@Entity
@Table(name = "elimination")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@PrimaryKeyJoinColumn(name="id")
public class Elimination extends Tournament implements Serializable {

    private static final long serialVersionUID = 1L;

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
            '}';
    }
}
