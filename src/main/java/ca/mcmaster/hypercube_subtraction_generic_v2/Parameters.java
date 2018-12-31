/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2; 

import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.BILLION;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.*;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.ZERO;
import ca.mcmaster.hypercube_subtraction_generic_v2.heuristics.BRANCHING_HEURISTIC_ENUM;

/**
 *
 * @author tamvadss
 */
public class Parameters {
     
    //public static final String MIP_FILENAME = "knapsackThreeTest.lp";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\knapsackThreeTest.lp";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\knapsackFourTest.lp";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\wnq.mps";
    
    //public static final String MIP_FILENAME = "F:\\temporary files here\\knapsackTinyInfeasible.lp.lp";
    //public static final String MIP_FILENAME = "knapsackThreeTest.lp";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\neos-807456.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\hanoi5.mps";
    //public static final String MIP_FILENAME = "f2000.mps";
    //public static final String MIP_FILENAME = "supportcase3.mps";
    //public static final String MIP_FILENAME = "F:\\\\temporary files here\\sorrell8.mps";
    //public static final String MIP_FILENAME = "F:\\\\temporary files here\\neos-beardy.mps";
    //public static final String MIP_FILENAME = "hanoi5.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\opm2-z10-s4.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\scpj4scip.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\stp3d.mps";
    //public static final String MIP_FILENAME ="seymour-disj-10.mps";
    
    
    //p6b hanoi5  neos807456 seymour bnatt500, supportcase3   , wnq, sorrell3 7 8 4  
    //
    //v150d30-2hopcds 2club200v  , f2000,    sts405, methanosarcina, ex1010-pi,  pythago7824, pythago7825 , ramos3
    //scpj4scip , scpk4, s1234    
    //
    // many SAT like constraints , seydisj10, academictimetablebig , reblock354 , opm2-z10-s4 , opm14 , opm7  with objective push
    //
    //supportcase10 , neos-abava  , z26 stp3d and wnq supportcase22 using dynamic serach
    
    //public static final String MIP_FILENAME ="v150d30-2hopcds.mps";
    //public static final String MIP_FILENAME ="sorrell8.mps";
    public static final String MIP_FILENAME = "sts405.mps";
    //public static final String MIP_FILENAME = "pythago7824.mps";
    //public static final String MIP_FILENAME = "sorrell3.mps";
    //public static final String MIP_FILENAME ="2club200v.mps";
    //public static final String MIP_FILENAME ="methanosarcina.mps";
    //p16 6public static final String MIP_FILENAME ="f2000.mps";
    //public static final String MIP_FILENAME ="hanoi5.mps";
    //public static final String MIP_FILENAME ="wnq.mps";
    //public static final String MIP_FILENAME ="neos807456.mps";
    //public static final String MIP_FILENAME ="F:\\\\temporary files here\\\\p6b.mps";
    //public static final String MIP_FILENAME ="pythago7824.mps";
    //public static final String MIP_FILENAME ="bnatt500.mps";
    //public static final String MIP_FILENAME ="neos-807456.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\wnq.mps";
    //public static final String MIP_FILENAME = "F:\\temporary files here\\neos-807456.mps.lp";
    
    public static final long PERF_VARIABILITY_RANDOM_SEED = 0;
    public static final java.util.Random  PERF_VARIABILITY_RANDOM_GENERATOR = new  java.util.Random  (PERF_VARIABILITY_RANDOM_SEED);
  
    public static final boolean USE_PURE_CPLEX = false;
    
    //collect the best vertex, and all adjacent vertices, and vertices adajacent to adjacent vertices, and so on
    //set to 0 to collect only the best vertex, and to a large numberto collect all
    public static final int NUM_ADJACENT_VERTICES_TO_COLLECT = ZERO;
    
    public static final boolean MERGE_COLLECTED_HYPERCUBES = false;
    public static final boolean ABSORB_COLLECTED_HYPERCUBES = false;
    
    public static final BRANCHING_HEURISTIC_ENUM HEURISTIC_ENUM = BRANCHING_HEURISTIC_ENUM.STEPPED_WEIGHT;
        
    public static final int LOOKAHEAD_LEVELS =  BILLION;

    //shuffle constraint during creation (for perf variablity) before arranging by desired order
    public static final boolean SHUFFLE_THE_CONSTRAINTS = false;
    //do you want to sort vars in a given constraint ? 
    public static final boolean SORT_THE_CONSTRAINT = false;
    //check duplicates?
    public static final boolean CHECK_FOR_DUPLICATES = false;
    //use this parameter to do multiple rounds of collection
    public static final int NUMBER_OF_AADITIONAL_HYPERCUBE_COLLECTION_ROUNDS =  ZERO;
}

