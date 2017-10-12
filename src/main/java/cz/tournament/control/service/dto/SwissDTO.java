/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.tournament.control.domain.Participant;
import cz.tournament.control.domain.Swiss;
import java.util.List;

/**
 *
 * @author Karolina Bozkova
 */
public class SwissDTO {
    private Swiss swiss;
    private List<Participant> seeding;

    public SwissDTO() {
    }

    public SwissDTO(Swiss swiss) {
        this.swiss = swiss;
    }

    public SwissDTO(List<Participant> seeding) {
        this.seeding = seeding;
    }
    
    public SwissDTO(Swiss swiss, List<Participant> seeding) {
        this.swiss = swiss;
        this.seeding = seeding;
    }

    public Swiss getSwiss() {
        return swiss;
    }

    public void setSwiss(Swiss swiss) {
        this.swiss = swiss;
    }

    public List<Participant> getSeeding() {
        return seeding;
    }

    public void setSeeding(List<Participant> seeding) {
        this.seeding = seeding;
    }
    
    public SwissDTO swiss(Swiss swiss){
        this.swiss = swiss;
        return this;
    }
    
    public SwissDTO seeding(List<Participant> seeding){
        this.seeding = seeding;
        return this;
    }
    
    private String seedingToString(){
        if(seeding == null || seeding.isEmpty()){
            return "[]";
        }
        int n = seeding.size();
        String str = "[";
        for (int i = 0; i < n-1; i++) {
            if(seeding.get(i) != null){
                str = str + seeding.get(i).getName() + ", ";
            }else{
                str = str + "null, ";
            }
        }
        //last
        if(seeding.get(n-1) != null){
            str = str + seeding.get(n-1).getName() + "]";
        }else{
            str = str + "null]";
        }
        return str;
    }

    @Override
    public String toString() {
        return "SwissDTO{" + "swiss tournament name= " + swiss.getName() + ", \n "
                + "seeding= " + seedingToString() + '}';
    }
    
    
    
}
