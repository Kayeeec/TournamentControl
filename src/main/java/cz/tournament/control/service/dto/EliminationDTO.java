package cz.tournament.control.service.dto;

import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Participant;
import java.util.List;

/**
 *
 * @author Karolina Bozkova
 */
public class EliminationDTO {
    private Elimination elimination;
    private List<Participant> seeding;

    public EliminationDTO() {
    }

    public EliminationDTO(Elimination elimination, List<Participant> participantSeeding) {
        this.elimination = elimination;
        this.seeding = participantSeeding;
    }

    public Elimination getElimination() {
        return elimination;
    }

    public void setElimination(Elimination elimination) {
        this.elimination = elimination;
    }
    
    public EliminationDTO elimination(Elimination elimination){
        this.elimination = elimination;
        return this;
    }

    public List<Participant> getSeeding() {
        return seeding;
    }

    public void setSeeding(List<Participant> seeding) {
        this.seeding = seeding;
    }
    
    public EliminationDTO seeding(List<Participant> participantSeeding){
        this.seeding = participantSeeding;
        return this;
    }

    @Override
    public String toString() {
        String eliminationStr;
        if(elimination== null){
            eliminationStr = "elimination: null";
        }else {
            eliminationStr = "elimination: {id: "+elimination.getId()+" name: "+elimination.getName()+"}";
        }
        String seedingStr = "seeding: [";
        if(seeding != null && seeding.size() > 0){
            for (int i = 0; i < seeding.size() - 1; i++) {
                seedingStr = seedingStr + seeding.get(i).getName() + ", ";
            }
            seedingStr = seedingStr + seeding.get(seeding.size()-1).getName() + "]";
        }else{
            seedingStr = seedingStr+"]";
        }
        return "EliminationDTO: {"+eliminationStr+", "+seedingStr+"}";
    }
    
    
    
    
    
    
    
    
}