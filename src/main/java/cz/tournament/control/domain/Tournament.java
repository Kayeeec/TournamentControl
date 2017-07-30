package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Tournament.
 */
@Entity
@Table(name = "tournament")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Inheritance(strategy=InheritanceType.JOINED)
public class Tournament implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "note")
    private String note;

    @Column(name = "points_for_winning")
    private Integer pointsForWinning;

    @Column(name = "points_for_losing")
    private Integer pointsForLosing;

    @Column(name = "points_for_tie")
    private Integer pointsForTie;

    @Column(name = "created")
    private ZonedDateTime created;

    @Column(name = "sets_to_win")
    private Integer setsToWin;

    @Column(name = "ties_allowed")
    private Boolean tiesAllowed = true;

    @Column(name = "playing_fields")
    private Integer playingFields;
    
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(unique = true)
    private SetSettings setSettings;

    @OneToMany(mappedBy = "tournament", fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnoreProperties({"tournament"})
    private Set<Game> matches = new HashSet<>();

    @ManyToOne
    private User user;

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "tournament_participants",
               joinColumns = @JoinColumn(name="tournaments_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="participants_id", referencedColumnName="id"))
    private Set<Participant> participants = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Tournament name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public Tournament note(String note) {
        this.note = note;
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPointsForWinning() {
        return pointsForWinning;
    }

    public Tournament pointsForWinning(Integer pointsForWinning) {
        this.pointsForWinning = pointsForWinning;
        return this;
    }

    public void setPointsForWinning(Integer pointsForWinning) {
        this.pointsForWinning = pointsForWinning;
    }

    public Integer getPointsForLosing() {
        return pointsForLosing;
    }

    public Tournament pointsForLosing(Integer pointsForLosing) {
        this.pointsForLosing = pointsForLosing;
        return this;
    }

    public void setPointsForLosing(Integer pointsForLosing) {
        this.pointsForLosing = pointsForLosing;
    }

    public Integer getPointsForTie() {
        return pointsForTie;
    }

    public Tournament pointsForTie(Integer pointsForTie) {
        this.pointsForTie = pointsForTie;
        return this;
    }

    public void setPointsForTie(Integer pointsForTie) {
        this.pointsForTie = pointsForTie;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public Tournament created(ZonedDateTime created) {
        this.created = created;
        return this;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public Integer getSetsToWin() {
        return setsToWin;
    }

    public Tournament setsToWin(Integer setsToWin) {
        this.setsToWin = setsToWin;
        return this;
    }

    public void setSetsToWin(Integer setsToWin) {
        this.setsToWin = setsToWin;
    }

    public Boolean getTiesAllowed() {
        return tiesAllowed;
    }

    public Tournament tiesAllowed(Boolean tiesAllowed) {
        this.tiesAllowed = tiesAllowed;
        return this;
    }

    public void setTiesAllowed(Boolean tiesAllowed) {
        this.tiesAllowed = tiesAllowed;
    }

    public Integer getPlayingFields() {
        return playingFields;
    }

    public Tournament playingFields(Integer playingFields) {
        if(playingFields<1){
            throw new IllegalArgumentException("Tournament.playingFields cannot "
                    + "be smaller than 1. Given argument: "+playingFields);
        }
        if(!participants.isEmpty() && playingFields > (participants.size()/2)){
            throw new IllegalArgumentException("Tournament.playingFields cannot "
                    + "be greater than number of participants divided by 2. Given argument = "+playingFields
                    +", number of participants = "+participants.size());
        }
        this.playingFields = playingFields;
        return this;
    }

    public void setPlayingFields(Integer playingFields) {
        this.playingFields = playingFields;
    }

    public Set<Game> getMatches() {
        return matches;
    }

    public Tournament matches(Set<Game> games) {
        this.matches = games;
        return this;
    }

    public Tournament addMatches(Game game) {
        this.matches.add(game);
        game.setTournament(this);
        return this;
    }

    public Tournament removeMatches(Game game) {
        this.matches.remove(game);
        game.setTournament(null);
        return this;
    }

    public void setMatches(Set<Game> games) {
        this.matches = games;
    }

    public User getUser() {
        return user;
    }

    public Tournament user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public Tournament participants(Set<Participant> participants) {
        this.participants = participants;
        return this;
    }

    public Tournament addParticipants(Participant participant) {
        this.participants.add(participant);
        return this;
    }

    public Tournament removeParticipants(Participant participant) {
        this.participants.remove(participant);
        return this;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    public SetSettings getSetSettings() {
        return setSettings;
    }

    public Tournament setSettings(SetSettings setSettings) {
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
        Tournament tournament = (Tournament) o;
        if (tournament.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, tournament.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Tournament{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", note='" + note + "'" +
            ", pointsForWinning='" + pointsForWinning + "'" +
            ", pointsForLosing='" + pointsForLosing + "'" +
            ", pointsForTie='" + pointsForTie + "'" +
            ", created='" + created + "'" +
            ", setsToWin='" + setsToWin + "'" +
            ", tiesAllowed='" + tiesAllowed + "'" +
            ", playingFields='" + playingFields + "'" +
            '}';
    }
}
