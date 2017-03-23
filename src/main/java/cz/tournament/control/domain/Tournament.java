package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import cz.tournament.control.domain.enumeration.TournamentType;

/**
 * A Tournament.
 */
@Entity
@Table(name = "tournament")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Tournament implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "note")
    private String note;

    @Min(value = 1)
    @Column(name = "number_of_mutual_matches")
    private Integer numberOfMutualMatches;

    @Column(name = "points_for_winning")
    private Integer pointsForWinning;

    @Column(name = "points_for_losing")
    private Integer pointsForLosing;

    @Column(name = "points_for_tie")
    private Integer pointsForTie;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_type", nullable = false)
    private TournamentType tournamentType = TournamentType.ALL_VERSUS_ALL;

    @OneToMany(mappedBy = "tournament")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Game> matches = new HashSet<>();

    @ManyToOne
    private User user;

    @ManyToMany
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

    public Integer getNumberOfMutualMatches() {
        return numberOfMutualMatches;
    }

    public Tournament numberOfMutualMatches(Integer numberOfMutualMatches) {
        this.numberOfMutualMatches = numberOfMutualMatches;
        return this;
    }

    public void setNumberOfMutualMatches(Integer numberOfMutualMatches) {
        this.numberOfMutualMatches = numberOfMutualMatches;
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

    public TournamentType getTournamentType() {
        return tournamentType;
    }

    public Tournament tournamentType(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
        return this;
    }

    public void setTournamentType(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
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
            ", numberOfMutualMatches='" + numberOfMutualMatches + "'" +
            ", pointsForWinning='" + pointsForWinning + "'" +
            ", pointsForLosing='" + pointsForLosing + "'" +
            ", pointsForTie='" + pointsForTie + "'" +
            ", tournamentType='" + tournamentType + "'" +
            '}';
    }
}
