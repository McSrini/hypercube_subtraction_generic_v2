/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2;

import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.*;
import static ca.mcmaster.hypercube_subtraction_generic_v2.CplexParameters.*;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Parameters.*;
import ca.mcmaster.hypercube_subtraction_generic_v2.collection.*;
import ca.mcmaster.hypercube_subtraction_generic_v2.collection.ConstraintAnchoredCollector;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.HyperCube;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.LowerBoundConstraint;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.VariableCoefficientTuple;
import ca.mcmaster.hypercube_subtraction_generic_v2.cplex.CplexSolver;
import ca.mcmaster.hypercube_subtraction_generic_v2.heuristics.BRANCHING_HEURISTIC_ENUM;
import ca.mcmaster.hypercube_subtraction_generic_v2.utils.MIPReader;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class Driver {
    
    public static Map<String, Double> objectiveFunctionMap;
    public  static List<LowerBoundConstraint> mipConstraintList ;
    public static  TreeMap<String, IloNumVar> mapOfAllVariablesInTheModel = new TreeMap<String, IloNumVar> ();
    public static Map<String, Integer> mapOfVariableFrequencyInConstraints = new HashMap<String ,Integer> ();
         
    private static CollectedInfeasibleHypercubeMap collectedHypercubeMap = new CollectedInfeasibleHypercubeMap();
    
    
    private static Logger logger=Logger.getLogger(Driver.class);
    static {
        logger.setLevel(LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  
                RollingFileAppender(layout,LOG_FOLDER+Driver.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);            
             
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    }
    
    public static void main(String[] args) throws Exception {
             
        logger.info("Start !") ;
        
        try {
            
            printParameters();
            
            IloCplex mip =  new IloCplex();
            mip.importModel(MIP_FILENAME);
            
            logger.info ("preparing allVariablesInModel ... ");
            for (IloNumVar var :MIPReader.getVariables(mip)){
                mapOfAllVariablesInTheModel.put (var.getName(), var );
            }
            System.out.println ("DONE preparing vars. Total is  "+ mapOfAllVariablesInTheModel.size());  
                 
            logger.info ("preparing constraints ... ");
            mipConstraintList= MIPReader.getConstraintsFast(mip);
            logger.info ("finding var frequency in constraints ... ");
            
            //find var frequency in constraints. This is used for sorting the variable order withiin the constraint
            if (SORT_THE_CONSTRAINT) initVariableFrequencyInConstraints();
            
            //sort every constraint expression
            logger.info ("sorting constraints ... ");
            for (LowerBoundConstraint lbc : mipConstraintList){
                //
                if (SORT_THE_CONSTRAINT) lbc.sort();
            }
            logger.info ("DONE preparing constraints ... ");
            System.out.println ("DONE preparing constraints. Total is  "+ mipConstraintList.size());  
            
            logger.info ("  preparing objective ... ");
            objectiveFunctionMap = MIPReader.getObjective(mip);
            //check that every var appears in the objective 
            int missingCount = ZERO;
            for (String var :mapOfAllVariablesInTheModel.keySet()){
                if (ZERO==Double.compare(ZERO ,objectiveFunctionMap.get(var))) {
                    //System.err.println("Variable "+ var + " does not occur in the objective.");
                    missingCount ++;
                    //exit(ONE);
                }
            }
            logger.warn("Count of vars missing from objective "+missingCount);
            
            
            //we have read the MIP, now start the actual work !
            
            //collect hypercubes
            boolean isMIPInfeasible= runOneRoundOfHypercubeCollection (false) ;
        
            int numberOfAdditionalCollectionRounds = ZERO;            
            for (;numberOfAdditionalCollectionRounds< Parameters.NUMBER_OF_AADITIONAL_HYPERCUBE_COLLECTION_ROUNDS;numberOfAdditionalCollectionRounds++ ){
                if (isMIPInfeasible) break;
                logger.info (" Starting additional hypercube collection round "+numberOfAdditionalCollectionRounds) ;
                
                isMIPInfeasible= runOneRoundOfHypercubeCollection (true) ;
            }
            
            
            if (isMIPInfeasible) {
                //no need for branching
                System.out.println("Mip is infeasible");
                exit(ZERO);
            } 
            
            if (ABSORB_COLLECTED_HYPERCUBES){
                //this call is needed to remove cubes marked as merged or absorbed
                collectedHypercubeMap.getCollectedCubes();
                //now we invoke absorb
                collectedHypercubeMap .absorb();
            }
            
            //collectedHypercubeMap.printCollectedHypercubes(true);
            
            
            //this call is needed to remove cubes marked as merged or absorbed
            collectedHypercubeMap.getCollectedCubes();
            
            //prepare the 2 maps we pass to heuristics
            TreeMap<Double, List<HyperCube>>   infeasibleHypercubeMap_ObjectiveKeyed = 
                    prepare_InfeasibleHypercubeMap_ObjectiveKeyed();
            TreeMap<Double, List<HyperCube>>   infeasibleHypercubeMap_SizeKeyed = 
                    prepare_InfeasibleHypercubeMap_SizeKeyed();
            
              
            //solve with cplex
            TreeMap<Double, List<HyperCube>>  infeasibleHypercubeMapToUse=
                    HEURISTIC_ENUM .equals(BRANCHING_HEURISTIC_ENUM.STEPPED_OBJECTIVE)?
                    infeasibleHypercubeMap_ObjectiveKeyed:
                    infeasibleHypercubeMap_SizeKeyed;
                    
            CplexSolver cplexSolver = new CplexSolver ( infeasibleHypercubeMapToUse) ;
            cplexSolver.solve( RAMP_UP_DURATION_HOURS,SOLUTION_DURATION_HOURS);
            cplexSolver.printSolution();
            
            
        } catch (Exception ex){
            System.err.println(ex) ;
            ex.printStackTrace();
        } finally {
            logger.info("Completed !") ;
        }
        
    }
    
    //collect hypercubes and return isInfeasible 
    private static boolean runOneRoundOfHypercubeCollection (boolean doShuffle) {
        boolean isMIPInfeasible= false;
        int numConstraintsCollectedFor=ZERO;
        
        for (LowerBoundConstraint lbc : mipConstraintList) {
            //System.out.println("Collected hypercubes for " + lbc.printMe());
            
            if (doShuffle) lbc.shuffle( );
            
            ConstraintAnchoredCollector collector = new ConstraintAnchoredCollector (lbc);
            collector.collectInfeasibleHypercubes();


            if (collector.collectedHypercubeMap.size()>ZERO){
                isMIPInfeasible = collectedHypercubeMap .addCubesAndCheckInfeasibility(collector.collectedHypercubeMap );
            }
            if (isMIPInfeasible) break;

            //System.out.println("\nCollected cubes");
            //collector.printCollectedHypercubes();
            //System.out.println("\nCumulative map ");
            //collectedHypercubeMap.printCollectedHypercubes(false);

            numConstraintsCollectedFor++;
            if (numConstraintsCollectedFor%(HUNDRED)==ZERO) {
                System.out.println("Collected hypercubes for this many constraints "+   numConstraintsCollectedFor /*+ "\n just collected "+lbc.name*/);
            }
        }
        
        return isMIPInfeasible;
    }
    
    private static  TreeMap<Double, List<HyperCube>> prepare_InfeasibleHypercubeMap_SizeKeyed (){
        TreeMap<Double, List<HyperCube>>   infeasibleHypercubeMap_SizeKeyed
                = new TreeMap<Double, List<HyperCube>>   ();
        
        //simply convert int to double
        for (Entry<Integer, List<HyperCube>> entry :collectedHypercubeMap.collectedHypercubes.entrySet()) {
            double key = entry.getKey();
            infeasibleHypercubeMap_SizeKeyed.put (key, entry.getValue()) ;
        }
        
        return infeasibleHypercubeMap_SizeKeyed;
    }
        
    private static  TreeMap<Double, List<HyperCube>> prepare_InfeasibleHypercubeMap_ObjectiveKeyed (){
        TreeMap<Double, List<HyperCube>>   infeasibleHypercubeMap_ObjectiveKeyed
                = new TreeMap<Double, List<HyperCube>>   ();
           
        for (List<HyperCube> cubes: collectedHypercubeMap.collectedHypercubes.values()                ){
            for (HyperCube cube: cubes){
                double bestVal =cube.getBestPossibleObjectiveValue();
                List<HyperCube> existingCubes =
                        infeasibleHypercubeMap_ObjectiveKeyed.get(bestVal);
                if (existingCubes==null)existingCubes= new ArrayList<HyperCube>();
                existingCubes.add(cube);
                infeasibleHypercubeMap_ObjectiveKeyed.put (bestVal, existingCubes );
            }
        } 
        
        return infeasibleHypercubeMap_ObjectiveKeyed;
    }
    
       
    private static void initVariableFrequencyInConstraints(){
        for (LowerBoundConstraint lbc : mipConstraintList){
            for (VariableCoefficientTuple tuple : lbc.constraintExpression){
                if (mapOfVariableFrequencyInConstraints.containsKey(tuple.varName)){
                    int currentFreq = mapOfVariableFrequencyInConstraints.get(tuple.varName);
                    mapOfVariableFrequencyInConstraints.put(tuple.varName, ONE +currentFreq);
                     
                }else {
                    mapOfVariableFrequencyInConstraints.put(tuple.varName, ONE);
                    
                }
            }
        }
        
        //print
        for (int freq=ONE; freq <= Collections.max(mapOfVariableFrequencyInConstraints.values()); freq++){
            System.out.print("for freq "+freq);
            int count = ZERO;
            for (Map.Entry<String, Integer> entry : mapOfVariableFrequencyInConstraints.entrySet()) {
                if (entry.getValue()==freq){
                    //System.out.print(entry.getKey());
                    count++;
                }
            }
            System.out.println(" count is "+count);
        }
    }
    
    
    private static void printParameters() {
        
        logger.info("MIP_FILENAME "+ MIP_FILENAME) ; 
        logger.info("MIP_EMPHASIS "+ MIP_EMPHASIS) ; 
        
        logger.info("MAX_THREADS "+ MAX_THREADS) ; 
        logger.info("FILE_STRATEGY "+ FILE_STRATEGY) ; 
   
        logger.info ("DISABLE_HEURISTICS "+ DISABLE_HEURISTICS) ;
        logger.info ("DISABLE_PROBING "+ DISABLE_PROBING) ;
        logger.info ("DISABLE_PRESOLVENODE "+ DISABLE_PRESOLVENODE) ;
        logger.info ("DISABLE_PRESOLVE "+ DISABLE_PRESOLVE) ;
        logger.info ("DISABLE_CUTS "+ DISABLE_CUTS) ;
        
        logger.info ("PERF_VARIABILITY_RANDOM_SEED "+ PERF_VARIABILITY_RANDOM_SEED) ;
    
     
        logger.info ("USE_PURE_CPLEX "+ USE_PURE_CPLEX) ;
        logger.info ("NUM_ADJACENT_VERTICES_TO_COLLECT "+ NUM_ADJACENT_VERTICES_TO_COLLECT) ;
        
        logger.info ("MERGE_COLLECTED_HYPERCUBES "+ MERGE_COLLECTED_HYPERCUBES) ;
        logger.info ("ABSORB_COLLECTED_HYPERCUBES "+ ABSORB_COLLECTED_HYPERCUBES) ;
        
        logger.info (" HEURISTIC_ENUM "+  HEURISTIC_ENUM) ;
        
        
        logger.info ("RAMP_UP_DURATION_HOURS "+ RAMP_UP_DURATION_HOURS) ;
        logger.info ("SOLUTION_DURATION_HOURS "+ SOLUTION_DURATION_HOURS) ;
        
        logger.info ("SHUFFLE_CONSTRAINT "+ SHUFFLE_THE_CONSTRAINTS) ;
        
        logger.info ("LOOKAHEAD_LEVELS "+ LOOKAHEAD_LEVELS) ;
        logger.info ("SORT_THE_CONSTRAINT "+ SORT_THE_CONSTRAINT) ;
        
        logger.info ("NUMBER_OF_HYPERCUBE_COLLECTION_ROUNDS "+ NUMBER_OF_AADITIONAL_HYPERCUBE_COLLECTION_ROUNDS) ;
        
        logger.info ("CHECK_FOR_DUPLICATES "+ CHECK_FOR_DUPLICATES) ;
        
        
    }
    
}
