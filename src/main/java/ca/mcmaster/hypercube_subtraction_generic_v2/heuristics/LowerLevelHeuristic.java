/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.heuristics;

import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.ONE;
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
 * only consider hypercubes at lowest level, i.e. those with the most fixings
 * Intuition is that the other hypercubes (which have fewer variable fixings ) ) will be met along the way of arriving at the lowest level hypercubes
 * We do not need to explicitly consider the hypercubes at the higher levels.
 * 
 * Also if some hypercubes are not met along the way because they do not share any variables in common with lowest hypercubes, then
 * after some time the lower hypercubes will slowly move up , and the higher hypercubes will move down and become the lowest level
 * 
 * Also note, if there are some hypercubes who do not have any variable in common with the lowest level hypercubes, then
 * those can be separated out and solved independently as another set of hypercubes
 * 
 * So the logic here is:
 * 1) find all the lowest level variables V_LOW
 * 2) find highest frequency variable X among V_LOW in the hypercubes above  that contain some variable other than  V_LOW
 * 3) if X exists and is unique, branch on X
 * 4) If X does not exist or there is a tie, break tie in favor of highest frequency among V_LOW
 * 
 * As a simplified version, we simply branch on highest frequency variable among the lowest level hypercubes.
 * 
 */
public class LowerLevelHeuristic extends BaseHeuristic{
    
    public LowerLevelHeuristic( ) {
       
    }

    
    
    public   List<String> getBranchingVariableSuggestions (){
        
        List<String> candidateVars = new ArrayList<String> ();
               
        List<HyperCube> cubesAtLowestLevel = infeasibleHypercubeMap.lastEntry().getValue();
        for (HyperCube cube: cubesAtLowestLevel){
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
        
        
        
        
        //return vars with highest freq      
        int maxFreq = Collections.max(this.scoreMap_Regular.values()) ;
        candidateVars.clear();
        for (Map.Entry<String , Integer> scoreEntry  : scoreMap_Regular.entrySet()){
            if (scoreEntry .getValue()==maxFreq){
                candidateVars.add( scoreEntry.getKey() );
            }
        }
        return candidateVars;
    }
  
}
