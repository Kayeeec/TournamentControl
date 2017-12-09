package cz.tournament.control.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import cz.tournament.control.domain.enumeration.TournamentType;

/**
 * A Combined.
 */
@Entity
@Table(name = "combined")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Combined implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String TOURNAMENT_TYPE = "combined";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "note")
    private String note;

    @Column(name = "created")
    private Instant created;

    @NotNull
    @Min(value = 0)
    @Column(name = "number_of_winners_to_playoff", nullable = false)
    private Integer numberOfWinnersToPlayoff;

    @NotNull
    @Min(value = 1)
    @Column(name = "number_of_groups", nullable = false)
    private Integer numberOfGroups;

    @Enumerated(EnumType.STRING)
    @Column(name = "playoff_type")
    private TournamentType playoffType;

    @Enumerated(EnumType.STRING)
    @Column(name = "in_group_tournament_type")
    private TournamentType inGroupTournamentType;

    @OneToMany()
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Participant> allParticipants = new HashSet<>();

    @OneToOne(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinColumn(unique = true)
    private Tournament playoff;

    @OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Tournament> groups = new HashSet<>();

    @ManyToOne
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Combined name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public Combined note(String note) {
        this.note = note;
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreated() {
        return created;
    }

    public Combined created(Instant created) {
        this.created = created;
        return this;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Integer getNumberOfWinnersToPlayoff() {
        return numberOfWinnersToPlayoff;
    }

    public Combined numberOfWinnersToPlayoff(Integer numberOfWinnersToPlayoff) {
        this.numberOfWinnersToPlayoff = numberOfWinnersToPlayoff;
        return this;
    }

    public void setNumberOfWinnersToPlayoff(Integer numberOfWinnersToPlayoff) {
        this.numberOfWinnersToPlayoff = numberOfWinnersToPlayoff;
    }

    public Integer getNumberOfGroups() {
        return numberOfGroups;
    }

    public Combined numberOfGroups(Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
        return this;
    }

    public void setNumberOfGroups(Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public TournamentType getPlayoffType() {
        return playoffType;
    }

    public Combined playoffType(TournamentType playoffType) {
        this.playoffType = playoffType;
        return this;
    }

    public void setPlayoffType(TournamentType playoffType) {
        this.playoffType = playoffType;
    }

    public TournamentType getInGroupTournamentType() {
        return inGroupTournamentType;
    }

    public Combined inGroupTournamentType(TournamentType inGroupTournamentType) {
        this.inGroupTournamentType = inGroupTournamentType;
        return this;
    }

    public void setInGroupTournamentType(TournamentType inGroupTournamentType) {
        this.inGroupTournamentType = inGroupTournamentType;
    }

    public Set<Participant> getAllParticipants() {
        return allParticipants;
    }

    public Combined allParticipants(Set<Participant> participants) {
        this.allParticipants = participants;
        return this;
    }

    public Combined addAllParticipants(Participant participant) {
        this.allParticipants.add(participant);
//        participant.setCombined(this);
        return this;
    }

    public Combined removeAllParticipants(Participant participant) {
        this.allParticipants.remove(participant);
//        participant.setCombined(null);
        return this;
    }

    public void setAllParticipants(Set<Participant> participants) {
        this.allParticipants = participants;
    }

    public Tournament getPlayoff() {
        return playoff;
    }

    public Combined playoff(Tournament tournament) {
        this.playoff = tournament;
        return this;
    }

    public void setPlayoff(Tournament tournament) {
        this.playoff = tournament;
    }

    public Set<Tournament> getGroups() {
        return groups;
    }

    public Combined groups(Set<Tournament> tournaments) {
        this.groups = tournaments;
        return this;
    }

    public Combined addGroups(Tournament tournament) {
        this.groups.add(tournament);
//        tournament.setCombined(this);
        return this;
    }

    public Combined removeGroups(Tournament tournament) {
        this.groups.remove(tournament);
//        tournament.setCombined(null);
        return this;
    }

    public void setGroups(Set<Tournament> tournaments) {
        this.groups = tournaments;
    }

    public User getUser() {
        return user;
    }

    public Combined user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.note);
        hash = 53 * hash + Objects.hashCode(this.created);
        hash = 53 * hash + Objects.hashCode(this.numberOfWinnersToPlayoff);
        hash = 53 * hash + Objects.hashCode(this.numberOfGroups);
        hash = 53 * hash + Objects.hashCode(this.playoffType);
        hash = 53 * hash + Objects.hashCode(this.inGroupTournamentType);
        hash = 53 * hash + Objects.hashCode(this.user);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Combined other = (Combined) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.note, other.note)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.created, other.created)) {
            return false;
        }
        if (!Objects.equals(this.numberOfWinnersToPlayoff, other.numberOfWinnersToPlayoff)) {
            return false;
        }
        if (!Objects.equals(this.numberOfGroups, other.numberOfGroups)) {
            return false;
        }
        if (this.playoffType != other.playoffType) {
            return false;
        }
        if (this.inGroupTournamentType != other.inGroupTournamentType) {
            return false;
        }
        if (!Objects.equals(this.user, other.user)) {
            return false;
        }
        return true;
    }
    
    private String printValueOrNull(Object o){
        if(o != null){
            return o.toString();
        }
        return "null";
    }

    @Override
    public String toString() {
        return "Combined{" + "id=" +  printValueOrNull(id)
                + ", name=" +  printValueOrNull(name)
                + ", note=" +  printValueOrNull(note)
                + ", created=" +  printValueOrNull(created)
                + ", numberOfWinnersToPlayoff=" +  printValueOrNull(numberOfWinnersToPlayoff)
                + ", numberOfGroups=" +  printValueOrNull(numberOfGroups)
                + ", playoffType=" +  printValueOrNull(playoffType)
                + ", inGroupTournamentType=" +  printValueOrNull(inGroupTournamentType)
                + ", user=" +  printValueOrNull(user) + System.lineSeparator()
                + ", allParticipants: "+ printAllParticipants()
                + '}';
    }

    private String printAllParticipants() {
        if(allParticipants == null) return "[]";
        String str = "[";
        for (Participant participant : allParticipants) {
            str = str + participant.getName() + ", ";
        }
        str = str + "]";
        return str;
    }

    
}
