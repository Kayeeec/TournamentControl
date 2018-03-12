/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.domain.exceptions;

import cz.tournament.control.domain.Participant;

/**
 *  Exception thrown when deleting participant that still is in any tournament.
 *  Shoult be handled on front end.
 * 
 * @author Karolina Bozkova
 */
public class ParticipantInTournamentException extends Exception{

    public ParticipantInTournamentException(String message) {
        super(message);
    }

    public ParticipantInTournamentException(Participant participant) {
        super("Cannot delete participant: "+participant.toString()+", is in at least one tournament.");
    }
    
    

    @Override
    public String toString() {
        return "ParticipantInTournamentException occurred: "+ this.getMessage();
    }
    
    
    
    
    
    
    
}
