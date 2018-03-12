package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.service.util.EvaluationParticipant;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Combined.
 */
@Entity
@Table(name = "combined")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Combined implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "combined_all_participants",
               joinColumns = @JoinColumn(name="combineds_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="all_participants_id", referencedColumnName="id"))
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
    
    private List<EvaluationParticipant> computeEvaluation(){
        Map<Long, EvaluationParticipant> map = new HashMap<>();
        
        //init map
        for (Participant participant : this.allParticipants) {
            map.put(participant.getId(), new EvaluationParticipant(participant));
        }
        
        //groups
        for (Tournament group : groups) {
            for (Game match : group.getMatches()) {
                 map = evaluateMatch(map, group, match);
            }
        }
        
        //playoff
        for (Game match : playoff.getMatches()) {
            map = evaluateMatch(map, playoff, match);
        }
        
        return new ArrayList<>(map.values());
    }
    
    @JsonIgnore
    public List<EvaluationParticipant> getRankedEvaluation(){
        List<EvaluationParticipant> evaluatedParticipants = this.computeEvaluation();
        if(evaluatedParticipants == null) return null;
        
        //sort
        Collections.sort(evaluatedParticipants, EvaluationParticipant.TotalWinsLosesScoreRatioDescendingComparator);
        
        //determine rank
        for (int i = 1; i < evaluatedParticipants.size(); i++) {
            EvaluationParticipant prev = evaluatedParticipants.get(i-1);
            EvaluationParticipant current = evaluatedParticipants.get(i);
            
            current.rank = prev.rank;
            if(current.notCompletelyEqual(prev)){
                current.rank += 1;
            }
            
        }
        return evaluatedParticipants;
    }
    
    
    
    private Map<Long, EvaluationParticipant> evaluateMatch(Map<Long, EvaluationParticipant> map, Tournament group, Game match){
        if(match.isFinished() && !match.getRivalA().isBye() && !match.getRivalB().isBye()){
            Map<String, Participant> winnerAndLoser = match.getWinnerAndLoser();
            Participant winner = winnerAndLoser.get("winner");
            Participant loser = winnerAndLoser.get("loser");
            if(winner == null || loser == null){ 
                //tie
                map.get(match.getRivalA().getId()).ties += 1;
                map.get(match.getRivalA().getId()).addToTotal(group.getPointsForTie());
                map.get(match.getRivalB().getId()).ties += 1;
                map.get(match.getRivalB().getId()).addToTotal(group.getPointsForTie());
            }else{
                map.get(winner.getId()).wins += 1;
                map.get(winner.getId()).addToTotal(group.getPointsForWinning());
                map.get(loser.getId()).loses +=1;
                map.get(loser.getId()).addToTotal(group.getPointsForLosing());
            }
            //score
            Map<String, Integer> score = match.getSumsOfScores(); //{A: 0, B: 0}
            map.get(match.getRivalA().getId()).score += score.get("A");
            map.get(match.getRivalA().getId()).rivalScore += score.get("B");

            map.get(match.getRivalB().getId()).score += score.get("B");
            map.get(match.getRivalB().getId()).rivalScore += score.get("A");
         }
        
        return map;
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
        return this;
    }

    public Combined removeAllParticipants(Participant participant) {
        this.allParticipants.remove(participant);
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
        return this;
    }

    public Combined removeGroups(Tournament tournament) {
        this.groups.remove(tournament);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Combined combined = (Combined) o;
        if (combined.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), combined.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
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
