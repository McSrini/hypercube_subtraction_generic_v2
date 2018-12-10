/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2;

/**
 *
 * @author tamvadss
 */
public class CplexParameters {
     
   
    
    public static final int MIP_EMPHASIS=   3; 
    public static final int MAX_THREADS= 32;  
    public static final int FILE_STRATEGY= 3;  
    
    
    
    public static final boolean DISABLE_HEURISTICS= true; 
    public static final boolean DISABLE_PROBING= false; 
    public static final boolean DISABLE_PRESOLVENODE = false ;
    public static final boolean DISABLE_PRESOLVE = false;
    public static final boolean DISABLE_CUTS = false;
    
    public static final int RAMP_UP_DURATION_HOURS= 2;  
    public static final int SOLUTION_DURATION_HOURS= 20*24 ;  //20 DAYS MAXIMUM
     
}
