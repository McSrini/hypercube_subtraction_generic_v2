/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.heuristics; 

import static ca.mcmaster.hypercube_subtraction_generic_v2.Parameters.*;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.*;
import static ca.mcmaster.hypercube_subtraction_generic_v2.heuristics.BRANCHING_HEURISTIC_ENUM.*;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class BranchingHeuristicFactory {
    
    public static BaseHeuristic getBranchingHeuristic (  ){
        BaseHeuristic heuristic = null;
        if (HEURISTIC_ENUM.equals (FEASIBILITY)){
            heuristic = new FeasibilityHeuristic (   ) ;
        } else if (HEURISTIC_ENUM.equals ( STEPPED_OBJECTIVE)) {
            heuristic = new  SteppedObjectiveHeuristic (   ) ;
        } else if (HEURISTIC_ENUM.equals ( LOWERLEVEL)) {
            heuristic = new  LowerLevelHeuristic  (   ) ;
        } else if (HEURISTIC_ENUM.equals(HEURISTIC_ENUM.STEPPED_WEIGHT)) {
            heuristic = new SteppedWeightHeuristic();
        }else {
            //default
            heuristic= new SimpleHeuristic (     );
        } 
        return heuristic;
    }
    
}

 