/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service.util;

import cz.tournament.control.domain.Participant;
import java.util.Comparator;

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
    
    public double computeTotal(double pointsForWinning, double pointsForLosing, double pointsForTie){
        
        this.total = (wins * pointsForWinning) + (ties * pointsForTie) - (loses * pointsForLosing);
        return total;
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
}
