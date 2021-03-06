package cz.tournament.control.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cz.tournament.control.domain.enumeration.TournamentType;
import cz.tournament.control.service.util.EvaluationParticipant;
import java.io.Serializable;
import java.time.ZonedDateTime;
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
    private Double pointsForWinning = 0d;

    @Column(name = "points_for_tie")
    private Double pointsForTie = 0d;

    @Column(name = "points_for_losing")
    private Double pointsForLosing = 0d;

    @Column(name = "in_combined")
    private Boolean inCombined = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_type")
    private TournamentType tournamentType;

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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private SetSettings setSettings;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    public Tournament() {
    }

    public Tournament(TournamentType tournamentType) {
        this.tournamentType = tournamentType;
    }
    
    
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Computes evaluation for each participant from finished non-bye matches. 
     * @JsonIgnore just to be sure.
     * @return List of EvaluationParticipant object for each participant in a tournament. 
     */
    private List<EvaluationParticipant> computeEvaluation(){
        Map<Long, EvaluationParticipant> map = new HashMap<>();
        
        //init map
        for (Participant participant : this.getParticipants()) {
            map.put(participant.getId(), new EvaluationParticipant(participant));
        }
        
        //gather number of wins, loses and ties from finished matches, ignores matches with BYE 
        for (Game match : matches) {
            if(match.isFinished() && !match.getRivalA().isBye() && !match.getRivalB().isBye()){
                Map<String, Participant> winnerAndLoser = match.getWinnerAndLoser();
                Participant winner = winnerAndLoser.get("winner");
                Participant loser = winnerAndLoser.get("loser");
                if(winner == null || loser == null){ 
                    //tie
                    map.get(match.getRivalA().getId()).ties += 1;
                    map.get(match.getRivalA().getId()).addToTotal(pointsForTie);
                    map.get(match.getRivalB().getId()).ties += 1;
                    map.get(match.getRivalB().getId()).addToTotal(pointsForTie);
                }else{
                    map.get(winner.getId()).wins += 1;
                    map.get(winner.getId()).addToTotal(pointsForWinning);
                    map.get(loser.getId()).loses +=1;
                    map.get(loser.getId()).addToTotal(pointsForLosing);
                }
                //score
                Map<String, Integer> score = match.getSumsOfScores(); //{A: 0, B: 0}
                map.get(match.getRivalA().getId()).score += score.get("A");
                map.get(match.getRivalA().getId()).rivalScore += score.get("B");

                map.get(match.getRivalB().getId()).score += score.get("B");
                map.get(match.getRivalB().getId()).rivalScore += score.get("A");
            }
        }
        return new ArrayList<>(map.values());
    }
    
    @JsonIgnore
    public List<EvaluationParticipant> getRankedEvaluation(){
        List<EvaluationParticipant> evaluatedParticipants = this.computeEvaluation();
        if(evaluatedParticipants == null) return null;
        
        //compute total on all
//        for (EvaluationParticipant ep : evaluatedParticipants) {
//            ep.computeTotal(pointsForWinning, pointsForLosing, pointsForTie);
//        }
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
//        for (EvaluationParticipant ep : evaluatedParticipants) {
//            ep.computeTotal(pointsForWinning, pointsForLosing, pointsForTie);
//        }
        //sort
        Collections.sort(evaluatedParticipants, EvaluationParticipant.TotalWinsLosesScoreRatioDescendingComparator);
        
        //extract first n participants 
        List<Participant> result = new ArrayList<>();
        int added = 0;
//        for (int i = 0; i < n; i++) {
//            result.add(evaluatedParticipants.get(i).getParticipant());
//        }
        for (EvaluationParticipant evaluatedParticipant : evaluatedParticipants) {
            result.add(evaluatedParticipant.getParticipant());
            added++;
            if(added > n) break;
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
    
    
    
    @JsonIgnore //though it might be useful on frontend
    public Boolean allMatchesFinished(){
        for (Game match : matches) {
            if(!match.allSetsFinished()){
                return false;
            }
        }
        return true;
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
            ", tournamentType='" + getTournamentType() + "'" +
            "}";
    }

    
    
    
}
