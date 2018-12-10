/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2; 

import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.BILLION;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.ZERO;
import ca.mcmaster.hypercube_subtraction_generic_v2.heuristics.BRANCHING_HEURISTIC_ENUM;

/**
 *
 * @author tamvadss
 */
public class Parameters {
     
    //public static final String MIP_FILENAME = "knapsackThreeTest.lp";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\knapsackThreeTest.lp";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\knapsackTinyInfeasible.lp.lp";
    //public static final String MIP_FILENAME = "knapsackThreeTest.lp";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\neos-952987.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\hanoi5.mps";
    //public static final String MIP_FILENAME = "p6b.mps";
    //public static final String MIP_FILENAME = "supportcase3.mps";
    //public static final String MIP_FILENAME = "F:\\\\temporary files here\\p6b.mps";
    //public static final String MIP_FILENAME = "hanoi5.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\opm2-z10-s4.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\scpj4scip.mps";
    //public static final String MIP_FILENAME ="seymour-disj-10.mps";
    
    
    //p6b hanoi5  neos807456 seymour bnatt500, supportcase3     
    //
    //v150d30-2hopcds 2club200v  , f2000,    sts405, methanosarcina, ex1010-pi,  pythago7824, pythago7825 , ramos3, 
    //scpj4scip , scpk4, s1234 
    //
    // many SAT like constraints , seydisj10, , reblock354 , opm2-z10-s4 , opm14 , opm7  with objective push
    //
    //supportcase10 , neos-abava,  sorrell3, wnq 
    
    //public static final String MIP_FILENAME ="hanoi5.mps";
    //public static final String MIP_FILENAME ="neos807456.mps";
    //public static final String MIP_FILENAME ="F:\\\\temporary files here\\\\p6b.mps";
    //public static final String MIP_FILENAME ="pythago7824.mps";
    //public static final String MIP_FILENAME ="bnatt500.mps";
    //public static final String MIP_FILENAME ="neos-807456.mps";
    public static final String MIP_FILENAME = "F:\\temporary files here\\wnq.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\neos-807456.mps.lp";
    
    public static final long PERF_VARIABILITY_RANDOM_SEED = 0;
    public static final java.util.Random  PERF_VARIABILITY_RANDOM_GENERATOR = new  java.util.Random  (PERF_VARIABILITY_RANDOM_SEED);
  
    public static final boolean USE_PURE_CPLEX = false;
    
    //collect the best vertex, and all adjacent vertices, and vertices adajacent to adjacent vertices, and so on
    //set to 0 to collect only the best vertex, and to a large numberto collect all
    public static final int NUM_ADJACENT_VERTICES_TO_COLLECT = BILLION;
    
    public static final boolean MERGE_COLLECTED_HYPERCUBES = true;
    public static final boolean ABSORB_COLLECTED_HYPERCUBES = true;
    
    public static final BRANCHING_HEURISTIC_ENUM HEURISTIC_ENUM = BRANCHING_HEURISTIC_ENUM.STEPPED_OBJECTIVE;
        
    public static final int LOOKAHEAD_LEVELS =  BILLION;

    //shuffle constraint during creation (for perf variablity) before arranging by desired order
    public static final boolean SHUFFLE_THE_CONSTRAINTS = false;
    //do you want to sort vars in a given constraint ?
    public static final boolean SORT_THE_CONSTRAINT = false;
}

