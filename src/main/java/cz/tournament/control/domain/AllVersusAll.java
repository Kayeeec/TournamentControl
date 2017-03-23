/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.domain;

import java.util.ArrayList;
import java.util.List;


public class AllVersusAll extends Tournament {

    public AllVersusAll() {
    }
    
    public String getType(){
        return "ALL VERSUS ALL";
    }
    
    private int getNumberOfRounds() {
        //při sudém počtu týmů je počet kol N–1, při lichém počtu je počet kol roven počtu týmů N
       int n = this.getParticipants().size();
       if (n % 2 == 0) return n-1;
       return n;
    }
    
    private Integer[] assignmentInit(int n){
        if(n%2==1){
            Integer[] result = new Integer[n + 1];
            for (int i = 0; i < n; i++){
                result[i]=i;
            }
            result[n] = null;
            return result;
        }
        Integer[] result = new Integer[n];
        for (int i = 0; i < n; i++){
            result[i]=i;
        }
        return result;
    }
    
    private void generateMatches(List<Participant> arr, Integer[] index, int round, int period){
        int n = index.length;
        for (int i = 0; i <= n/2; i++){
            if (index[i] != null && index[n-1-i] != null){
                Game match = new Game(period, round, arr.get(index[i]), arr.get(index[n-1-i]), this);
                this.addMatches(match);  
            }  
        }
    }
    
    private Integer[] shiftIndices(Integer[] index){
        int n = index.length;
        Integer[] result = new Integer[n];
        
        for (int i = 1; i <= n-2; i++){
            result[i+1]=index[i];
        }
        result[1]=index[n-1];
        
        return result;
    }
    
    public void generateAssignment(){
        int numberOfPeriods = getNumberOfMutualMatches();
        
        List<Participant> arr = new ArrayList<>(this.getParticipants());
        Integer[] index = assignmentInit(arr.size());
        
        for (int period = 1; period <= numberOfPeriods; period++) {
            for (int round = 1; round <= getNumberOfRounds(); round++){
                generateMatches(arr, index, round, period);
                index = shiftIndices(index);
            }
        }  
    }

    
    
    
}
