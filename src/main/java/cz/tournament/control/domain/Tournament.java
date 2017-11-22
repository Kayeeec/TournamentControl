package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Column(name = "created")
    private ZonedDateTime created;

    @Column(name = "sets_to_win")
    private Integer setsToWin;

    @Column(name = "ties_allowed")
    private Boolean tiesAllowed = true;

    @Column(name = "playing_fields")
    private Integer playingFields;

    @Column(name = "points_for_winning")
    private Double pointsForWinning;

    @Column(name = "points_for_tie")
    private Double pointsForTie;

    @Column(name = "points_for_losing")
    private Double pointsForLosing;

    @Column(name = "in_combined")
    private Boolean inCombined = false;

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

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(unique = true)
    private SetSettings setSettings;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    private List<EvaluationParticipant> computeEvaluation(){
        Map<Long, EvaluationParticipant> map = new HashMap<>();
        
        //gather number of wins, loses and ties
        for (Game match : matches) {
            if(!match.isFinished()){
                return null;
            }
            //add rivals to map if not bye - saves us a cycle
            if(!match.getRivalA().isBye() && !map.containsKey(match.getRivalA().getId()) ){
                map.put(match.getRivalA().getId(), new EvaluationParticipant(match.getRivalA()) );
            }
            if(!match.getRivalB().isBye() && !map.containsKey(match.getRivalB().getId()) ){
                map.put(match.getRivalB().getId(), new EvaluationParticipant(match.getRivalB()) );
            }
            
            Map<String, Participant> winnerAndLoser = match.getWinnerAndLoser();
            Participant winner = winnerAndLoser.get("winner");
            Participant loser = winnerAndLoser.get("loser");
            if(winner == null || loser == null){ 
                //tie
                map.get(match.getRivalA().getId()).ties += 1;
                map.get(match.getRivalB().getId()).ties += 1;
            }else{
                map.get(winner.getId()).wins += 1;
                map.get(loser.getId()).loses +=1;
            }
        }
        return new ArrayList<>(map.values());
    }
    
    /**
     * If all games are finished returns given number of participants sorted by their place:
     *     [1st, 2nd, 3rd ...]
     * 
     * Uses EvaluationParticipant class.
     * 
     * @param n - number of participants to return, not bigger that number of participants
     * @return null if tournament not finished (has unfinished match), n participants otherwise
     */
    public List<Participant> getNWinners(int n){
        if(n > participants.size()){
            throw new IllegalArgumentException("Cannot return more winners than there are players - n > participants.size()");
        }
        
        List<EvaluationParticipant> evaluatedParticipants = computeEvaluation();
        if(evaluatedParticipants == null) return null;
        
        //compute total on all
        for (EvaluationParticipant ep : evaluatedParticipants) {
            ep.computeTotal(pointsForWinning, pointsForLosing, pointsForTie);
        }
        //sort
        Collections.sort(evaluatedParticipants, EvaluationParticipant.TotalWinsLosesTiesDescendingComparator);
        
        //extract first n participants 
        List<Participant> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(evaluatedParticipants.get(i).getParticipant());
        }
        return result;
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

    public Double getPointsForWinning() {
        return pointsForWinning;
    }

    public Tournament pointsForWinning(Double pointsForWinning) {
        this.pointsForWinning = pointsForWinning;
        return this;
    }

    public void setPointsForWinning(Double pointsForWinning) {
        this.pointsForWinning = pointsForWinning;
    }

    public Double getPointsForTie() {
        return pointsForTie;
    }

    public Tournament pointsForTie(Double pointsForTie) {
        this.pointsForTie = pointsForTie;
        return this;
    }

    public void setPointsForTie(Double pointsForTie) {
        this.pointsForTie = pointsForTie;
    }

    public Double getPointsForLosing() {
        return pointsForLosing;
    }

    public Tournament pointsForLosing(Double pointsForLosing) {
        this.pointsForLosing = pointsForLosing;
        return this;
    }

    public void setPointsForLosing(Double pointsForLosing) {
        this.pointsForLosing = pointsForLosing;
    }

    public Boolean isInCombined() {
        return inCombined;
    }

    public Tournament inCombined(Boolean inCombined) {
        this.inCombined = inCombined;
        return this;
    }

    public void setInCombined(Boolean inCombined) {
        this.inCombined = inCombined;
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
    
    public String getTournamentType(){
        return "tournament";
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
        Tournament other = (Tournament) o;
        if (other.getId() == null || getId() == null) {
            return false;
        }
//        if(this.id != null && other.getId() != null){
//            return Objects.equals(getId(), other.getId());
//        }
        return Objects.equals(created, other.getCreated())
                && Objects.equals(matches, other.getMatches())
                && Objects.equals(name, other.getName())
                && Objects.equals(note, other.getNote())
                && Objects.equals(participants, other.getParticipants())
                && Objects.equals(playingFields, other.getPlayingFields())
                && Objects.equals(pointsForLosing, other.getPointsForLosing())
                && Objects.equals(pointsForTie, other.getPointsForTie())
                && Objects.equals(pointsForWinning, other.getPointsForWinning())
                && Objects.equals( setSettings, other.getSetSettings())
                && Objects.equals(setsToWin, other.getSetsToWin())
                && Objects.equals(tiesAllowed, other.getTiesAllowed())
                && Objects.equals(user, other.getUser())
                && Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 59;
        int result = 1;
        result = prime * result + Objects.hashCode(created);
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(note);
        result = prime * result + Objects.hashCode(playingFields);
        result = prime * result + Objects.hashCode(pointsForLosing);
        result = prime * result + Objects.hashCode(pointsForTie);
        result = prime * result + Objects.hashCode(pointsForWinning);
        result = prime * result + Objects.hashCode(setSettings);
        result = prime * result + Objects.hashCode(setsToWin);
        result = prime * result + Objects.hashCode(tiesAllowed);
        result = prime * result + Objects.hashCode(user);
        return result;
    }

    @Override
    public String toString() {
        return "Tournament{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", note='" + getNote() + "'" +
            ", created='" + getCreated() + "'" +
            ", setsToWin='" + getSetsToWin() + "'" +
            ", tiesAllowed='" + getTiesAllowed()+ "'" +
            ", playingFields='" + getPlayingFields() + "'" +
            ", pointsForWinning='" + getPointsForWinning() + "'" +
            ", pointsForTie='" + getPointsForTie() + "'" +
            ", pointsForLosing='" + getPointsForLosing() + "'" +
            ", inCombined='" + isInCombined() + "'" +
            "}";
    }
}
