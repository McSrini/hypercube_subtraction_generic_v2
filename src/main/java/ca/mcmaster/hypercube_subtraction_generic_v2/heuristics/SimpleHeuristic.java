/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.heuristics;

import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.ONE;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.ZERO;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Parameters.LOOKAHEAD_LEVELS;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.HyperCube;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 * 
 * simply count highest frequency variable among all hypercubes
 * 
 */
public class SimpleHeuristic extends BaseHeuristic{
    
    public SimpleHeuristic (){
        
    }

    
       
    public   List<String> getBranchingVariableSuggestions (){
        
        List<String> candidateVars = new ArrayList<String> ();
               
         
        for (Map.Entry <Double, List<HyperCube>> entry: infeasibleHypercubeMap.entrySet()){
            
 
            
            List<HyperCube> cubesAtThisLevel = entry.getValue();
            for (HyperCube cube: cubesAtThisLevel){
                for (String var : cube.zeroFixingsMap.keySet()){
                     
                    
                    Integer currentScore =scoreMap_Regular.get(var);
                    if (null==currentScore){
                        scoreMap_Regular.put (var, ONE) ;
                    }else {
                        scoreMap_Regular.put (var, currentScore+ONE) ;
                    }
                }
                for (String var : cube.oneFixingsMap.keySet()){
                     
                    
                    Integer currentScore =scoreMap_Regular.get(var);
                    if (null==currentScore){
                        scoreMap_Regular.put (var, ONE) ;
                    }else {
                        scoreMap_Regular.put (var, currentScore+ONE) ;
                    }
                }
            } 
            
             
            
            
        }
        
        
        //return vars with highest freq
        //System.out.println( "\n\ncandidateVars size is = "+candidateVars.size()+"\n\n");
        return candidateVars;
    }
    
    
     
    
}
