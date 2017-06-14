package cz.tournament.control.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Participant.
 */
@Entity
@Table(name = "participant")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Participant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(unique = true)
    private Player player;

    @OneToOne
    @JoinColumn(unique = true)
    private Team team;

    @ManyToOne(optional = false)
    @NotNull
    private User user;

    public Participant() {
    }

    public Participant(Player player, User user) {
        this.player = player;
        this.user = user;
    }

    public Participant(Team team, User user) {
        this.team = team;
        this.user = user;
    }
    
    public String getName(){
        if(this.player != null) return player.getName();
        if(this.team != null) return team.getName();
        return "BYE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public Participant player(Player player) {
        this.player = player;
        return this;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Team getTeam() {
        return team;
    }

    public Participant team(Team team) {
        this.team = team;
        return this;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public User getUser() {
        return user;
    }

    public Participant user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Participant participant = (Participant) o;
        if (participant.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, participant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String result = "Participant { id="+id;
        if(this.player != null){
            String concat = result.concat(", " + player.toString() + " }");
            return concat;
            
        }
        if(this.team != null){
            String concat = result.concat(", " + team.toString() + " }");
            return concat;
        }
        String concat = result.concat(", BYE }");
        return concat;
    }
    
    public boolean isBye(){
        return Objects.equals(this.team, null) && Objects.equals(this.player, null);
    }
    
//    public boolean getBye(){
//        return Objects.equals(this.team, null) && Objects.equals(this.player, null);
//    }
//    public void setBye(){
//        
//    }
}
