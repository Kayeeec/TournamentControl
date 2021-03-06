package cz.tournament.control.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Team.
 */
@Entity
@Table(name = "team")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "note")
    private String note;

    @ManyToOne
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "team_members",
               joinColumns = @JoinColumn(name="teams_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="members_id", referencedColumnName="id"))
    private Set<Player> members = new HashSet<>();

    public Team() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Team name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public Team note(String note) {
        this.note = note;
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getUser() {
        return user;
    }

    public Team user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Player> getMembers() {
        return members;
    }

    public Team members(Set<Player> players) {
        this.members = players;
        return this;
    }

    public Team addMembers(Player player) {
        this.members.add(player);
        player.getTeams().add(this);
//        player.addTeams(this);
        return this;
    }

    public Team removeMembers(Player player) {
        this.members.remove(player);
        player.getTeams().remove(this);
//        player.removeTeams(this);
        return this;
    }

    public void setMembers(Set<Player> players) {
        this.members = players;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Team team = (Team) o;
        if(this.id == null || team.getId() == null){
            return false;
        }
//        if(this.id != null && team.getId() != null){
//            return Objects.equals(this.id,team.getId());
//        }
        
        return Objects.equals(this.name,team.getName())
                && Objects.equals(this.user,team.getUser())
                && Objects.equals(this.id,team.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String members = "";
        for (Player member : this.getMembers()) {
            members = members + "("+ member.getId() +", "+ member.getName()+"), ";
        }
        return "Team{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", note='" + note + "'" +
            ", members=[" + members + "]" +
            '}';
    }
}
