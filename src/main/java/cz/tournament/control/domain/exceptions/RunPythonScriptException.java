/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.tournament.control.domain.exceptions;

/**
 *
 * @author Karolina Bozkova
 */
public class RunPythonScriptException extends Exception{

    public RunPythonScriptException() {
    }

    @Override
    public String toString() {
        return "RunPythonScriptException occurred: something went wrong while executing python scrypt.";
    }
    
}
