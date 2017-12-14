/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service.util;

import cz.tournament.control.domain.Participant;
import java.util.Comparator;
import java.util.Objects;

/**
 * Temporary object used for computing statistics
 * 
 * @author Karolina Bozkova
 */
public class EvaluationParticipant {
    public Participant participant;
    public Integer wins = 0;
    public Integer loses = 0;
    public Integer ties = 0;
    public Double total = 0.;
    public Double score = 0.;
    public Double rivalScore = 0.;
    public Integer rank = 1;

    public EvaluationParticipant(Participant participant) {
        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }
    
    public double computeTotal(Double pointsForWinning, Double pointsForLosing, Double pointsForTie){
        pointsForWinning = pointsForWinning == null ? 0. : pointsForWinning;
        pointsForLosing = pointsForLosing == null ? 0. : pointsForLosing;
        pointsForTie = pointsForTie == null ? 0. : pointsForTie;
        
        this.total = (wins * pointsForWinning) + (ties * pointsForTie) - (loses * pointsForLosing);
        return total;
    }
    
    public void addToTotal(Double points){
        points = points == null ? 0. : points; 
        this.total += points;
    }
    
    public static Comparator<EvaluationParticipant> TotalWinsLosesScoreRatioDescendingComparator
            = new Comparator<EvaluationParticipant>() {
        @Override
        public int compare(EvaluationParticipant ev2, EvaluationParticipant ev1) {
            if (ev1 == null || ev2 == null) {
                throw new NullPointerException("EvaluationParticipant.compareTo(EvaluationParticipant o) : o is null");
            }
            
            int byTotal = ev1.total.compareTo(ev2.total);
            if(byTotal != 0) return byTotal;
            
            int byWins = ev1.wins.compareTo(ev2.wins);
            if(byWins != 0) return byWins;
            
            int byLoses = ev2.loses.compareTo(ev1.loses); //we want the least loses
            if(byLoses != 0) return byLoses;
            
            Double ev1_scoreRatio = ev1.score/ev1.rivalScore;
            Double ev2_scoreRatio = ev2.score/ev2.rivalScore;
            return ev1_scoreRatio.compareTo(ev2_scoreRatio);
        }

    };

    @Override
    public String toString() {
        return "EvaluationParticipant{ rank: "+rank+
                ", "+participant.getName()
                +", wins: "+wins
                +", loses: "+loses
                +", ties: "+ties
                +", score: "+score
                +", rivalScore: "+rivalScore
                +", total: "+total
                +"}";
    }
    
    public boolean notCompletelyEqual(EvaluationParticipant prev) {
        return !Objects.equals(prev.loses, this.loses) 
                || !Objects.equals(prev.total, this.total) 
                || !Objects.equals(prev.wins, this.wins)
                || scoreComparison(prev)
                ;
    }
    
    private boolean scoreComparison(EvaluationParticipant prev) {
        Double prevRatio;
        if(prev.rivalScore == 0){
            prevRatio = prev.score;
        }else{
            prevRatio = prev.score/prev.rivalScore;
        }
        Double currentRatio;
        if(this.rivalScore == 0){
            currentRatio = this.score;
        }else{
            currentRatio = this.score/this.rivalScore;
        }
        return !Objects.equals(prevRatio,currentRatio);
    }
    
    
}
