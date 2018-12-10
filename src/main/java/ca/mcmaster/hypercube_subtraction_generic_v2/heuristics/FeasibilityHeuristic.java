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
 * pick variables which leave 1 side with the fewest infeasible cubes
 * 
 */
public class FeasibilityHeuristic extends BaseHeuristic{
    
    public FeasibilityHeuristic ( ){
        
    }

    //overrides base implementation
    public List<String> getBranchingVariableSuggestions() {
        
        for (List<HyperCube> cubeList : this.infeasibleHypercubeMap.values()){
            for (HyperCube cube: cubeList){
                for (String var : cube.oneFixingsMap.keySet()){
                    Integer currentScore =scoreMap_Regular.get(var);
                    if (null==currentScore){
                        scoreMap_Regular.put (var, ONE) ;
                    }else {
                        scoreMap_Regular.put (var, currentScore+ONE) ;
                    }
                }
                for (String var : cube.zeroFixingsMap.keySet()){
                    Integer currentScore = scoreMap_Complimented.get(var);
                    if (null==currentScore){
                        scoreMap_Complimented .put (var, ONE) ;
                    }else {
                        scoreMap_Complimented .put (var, currentScore+ONE) ;
                    }
                }
            }
        }
        
        
        int lowestFreq_HIGHSIDE= Collections.min( this.scoreMap_Regular.values());
        int lowestFreq_LOWSIDE= Collections.min( this.scoreMap_Complimented.values());
        List<String> candidateVars = new ArrayList<String> ();
        
        if (lowestFreq_HIGHSIDE<=lowestFreq_LOWSIDE){
            for (Map.Entry<String , Integer> entry : scoreMap_Regular.entrySet()){
                if (entry.getValue()==lowestFreq_HIGHSIDE){
                    candidateVars.add(entry.getKey() );
                }
            }
        }
        if (lowestFreq_HIGHSIDE>=lowestFreq_LOWSIDE){
            for (Map.Entry<String , Integer> entry : scoreMap_Complimented.entrySet()){
                if (entry.getValue()==lowestFreq_LOWSIDE ){
                    candidateVars.add(entry.getKey() );
                }
            }
        }
                
        return candidateVars;
    }
    
}
