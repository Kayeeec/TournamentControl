/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service.util;

import cz.tournament.control.domain.Game;
import cz.tournament.control.domain.Participant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *  Utility class used for computing weight matrice in SwissService.generateNextRound()
 * 
 * @author Karolina Bozkova
 */
public class SwissParticipant {
    private Long id;
    private Participant participant;
    private Set<Game> games = new HashSet<>();
    private Set<Participant> rivals = new HashSet<>();
    private String colorStr = "";
    private Double points = 0.0;

    public SwissParticipant() {
    }
    
    public SwissParticipant(Participant participant, Set<Game> games, Set<Participant> rivals, String colorStr, Double points) {
        this.participant = participant;
        this.id = participant.getId();
        this.games = games;
        this.rivals = rivals;
        this.colorStr = colorStr;
        this.points = points;
    }

    public Long getId() {
        return id;
    }
    
    public void appendColorString(String str){
        this.colorStr = this.colorStr + str;
    }
    
    public void incrPoints(Double by){
        this.points += by;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
        this.id = participant.getId();
    }
    
    public SwissParticipant participant(Participant participant){
        this.participant = participant;
        this.id = participant.getId();
        return this;
    }

    public Set<Game> getGames() {
        return games;
    }

    public void setGames(Set<Game> games) {
        this.games = games;
    }
    
    public SwissParticipant games(Set<Game> games){
        this.games = games;
        return this;
    }
    
    public Set<Game> addGame(Game game){
        this.games.add(game);
        return this.games;
    }
    
    public Set<Game> removeGame(Game game){
        this.games.remove(game);
        return this.games;
    }

    public Set<Participant> getRivals() {
        return rivals;
    }

    public void setRivals(Set<Participant> rivals) {
        this.rivals = rivals;
    }
    
    public SwissParticipant rivals(Set<Participant> rivals){
        this.rivals = rivals;
        return this;
    }
    
    public Set<Participant> addRival(Participant rival){
        this.rivals.add(rival);
        return this.rivals;
    }
    
    public Set<Participant> removeRival(Participant rival){
        this.rivals.remove(rival);
        return this.rivals;
    }

    public String getColorStr() {
        return colorStr;
    }

    public void setColorStr(String colorStr) {
        this.colorStr = colorStr;
    }
    
    public SwissParticipant colorStr(String colorStr){
        this.colorStr = colorStr;
        return this;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }
    
    public SwissParticipant points(Double points){
        this.points = points;
        return this;
    }

    @Override
    public int hashCode() {
//        int hash = 5;
//        hash = 53 * hash + Objects.hashCode(this.participant);
//        return hash;
            return this.participant.getId().intValue();
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
        final SwissParticipant other = (SwissParticipant) obj;
        if (!Objects.equals(this.participant, other.participant)) {
            return false;
        }
        return true;
    }
    
    public static Comparator<SwissParticipant> IdComparator
            = new Comparator<SwissParticipant>() {
        @Override
        public int compare(SwissParticipant sp1, SwissParticipant sp2) {
            
            if (sp1 == null || sp2 == null) {
                throw new NullPointerException("Game.compareTo(Game o) : o is null");
            }
            return sp1.getId().compareTo(sp2.getId());
        }

    };
    
    
    
    
    
}
