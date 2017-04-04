/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service;

import cz.tournament.control.domain.Participant;

/**
 *
 * @author Karolina Bozkova
 */
public class EvalClassic {
    public Participant participant;
    public int matches;
    public int wins;
    public int loses;
    public int scoreHis;
    public int scoreRival;
    public int ties;
    public int points;
    public int rank;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public int getMatches() {
        return matches;
    }

    public void setMatches(int matches) {
        this.matches = matches;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLoses() {
        return loses;
    }

    public void setLoses(int loses) {
        this.loses = loses;
    }

    public int getScoreHis() {
        return scoreHis;
    }

    public void setScoreHis(int scoreHis) {
        this.scoreHis = scoreHis;
    }

    public int getScoreRival() {
        return scoreRival;
    }

    public void setScoreRival(int scoreRival) {
        this.scoreRival = scoreRival;
    }

    public int getTies() {
        return ties;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    
    
    
    
}
