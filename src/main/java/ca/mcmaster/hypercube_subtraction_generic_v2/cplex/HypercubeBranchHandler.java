/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.cplex; 
 
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.*;
import ca.mcmaster.hypercube_subtraction_generic_v2.Driver;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Parameters.HEURISTIC_ENUM;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.HyperCube;
import ca.mcmaster.hypercube_subtraction_generic_v2.heuristics.*;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.IntegerFeasibilityStatus; 
import ilog.cplex.IloCplex.NodeId;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Parameters.PERF_VARIABILITY_RANDOM_GENERATOR;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.HyperCubeFilterResult;

/**
 *
 * @author tamvadss
 */
public class HypercubeBranchHandler extends IloCplex.BranchCallback{
    
    private final TreeMap<Double, List<HyperCube>>  collectedHypercubes;
    
     
    private static Logger logger=Logger.getLogger(HypercubeBranchHandler.class);
    static {
        logger.setLevel(LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender appender = new  RollingFileAppender(layout,
                    LOG_FOLDER+HypercubeBranchHandler.class.getSimpleName()+ LOG_FILE_EXTENSION);
            appender.setMaxBackupIndex(SIXTY);
            logger.addAppender(appender);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
    
    public HypercubeBranchHandler (       TreeMap<Double, List<HyperCube>>   infeasibleHypercubeMap ) {
        this. collectedHypercubes =infeasibleHypercubeMap;
         
    }
    
    
    protected void main() throws IloException {
        if ( getNbranches()> ZERO ){  
             
            NodeId thisNodeID = getNodeId(); 
            boolean isMipRoot = ( thisNodeID.toString()).equals( MIP_ROOT_ID);
                       
            //get the node attachment for this node, any child nodes will accumulate the branching conditions
            
            if (isMipRoot){
                //root of mip
                
                NodePayload data = new NodePayload (  );
                data.infeasibleHypercubesMap=this.collectedHypercubes;
                setNodeData(data);                
            } 
            
            NodePayload nodeData = (NodePayload) getNodeData();
            
            //the first thing to do in the branch callback is to find the var fixings, and 
            //filter our hypercube collection
            TreeMap<String, Boolean> thisNodesVarFixings = this.getCplexFixedVars();
            
            //this.printVarFixings(thisNodesVarFixings);
            
            
            thisNodesVarFixings.keySet().removeAll(nodeData.parentVarFixings.keySet());
            //this.printVarFixings(thisNodesVarFixings);
            
            //this.printInfeasibleHyperCubeMap(nodeData.infeasibleHypercubesMap );
            
            
            //for these additional fixings, create a local infeasible hypercube map
            HyperCubeFilterResult filterResult  = 
                    getFilteredHypercubes(nodeData.infeasibleHypercubesMap, thisNodesVarFixings);
            
            
            
            if (filterResult.isNodeInfeasibilityDetected) {
                logger.warn("Infeasibility detected for node "+ thisNodeID);
                               
                prune();                
            } else {

                //this.printInfeasibleHyperCubeMap(filteredHypercubes );

                //restore var fixings, these will be passed to both kids
                for (Entry <String, Boolean> parentFixing: nodeData.parentVarFixings.entrySet()){                
                    thisNodesVarFixings .put(parentFixing.getKey(),parentFixing.getValue() );
                }

                //this.printVarFixings(thisNodesVarFixings);


                //now pass the filtered hypercubes to the branching heuristic
                BaseHeuristic branchingHeuristic =BranchingHeuristicFactory.getBranchingHeuristic();
                branchingHeuristic.infeasibleHypercubeMap=filterResult.filteredHypercubes;
                
                //logger.warn ("get branch var for node "+ thisNodeID.toString());
                
                List<String> candidateBranchingVars =branchingHeuristic.getBranchingVariableSuggestions();

                if (candidateBranchingVars!=null && candidateBranchingVars.size()!=ZERO) {

                    //pick one candidate at random
                    int randomPosition = PERF_VARIABILITY_RANDOM_GENERATOR.nextInt(candidateBranchingVars.size());
                    String branchingVarDecision = candidateBranchingVars.get(randomPosition );

                    // vars needed for child node creation 
                    IloNumVar[][] vars = new IloNumVar[TWO][] ;
                    double[ ][] bounds = new double[TWO ][];
                    IloCplex.BranchDirection[ ][]  dirs = new  IloCplex.BranchDirection[ TWO][];
                    getArraysNeededForCplexBranching(branchingVarDecision, vars , bounds , dirs);

                    //create both kids, pass on infeasible hypercubes from parent      

                    double lpEstimate = getObjValue();

                    NodePayload zeroChildData = new NodePayload (  );
                    zeroChildData.infeasibleHypercubesMap= filterResult.filteredHypercubes;
                    zeroChildData.parentVarFixings= thisNodesVarFixings;
                    NodeId zeroChildID =  makeBranch( vars[ZERO],  bounds[ZERO],dirs[ZERO],  lpEstimate  , zeroChildData );

                    NodePayload oneChildData = zeroChildData; /*new NodePayload (  );                
                    oneChildData.parentVarFixings= thisNodesVarFixings;
                    oneChildData.infeasibleHypercubesMap=filteredHypercubes;*/
                    NodeId oneChildID = makeBranch( vars[ONE],  bounds[ONE],dirs[ONE],   lpEstimate, oneChildData );
                    
                    //logger.warn(" parent and 2 kids "+ thisNodeID + " "+zeroChildID + " "+ oneChildID + " "+branchingVarDecision);
                     

                }else {
                    //do nothing, take cplex default branching
                    logger.warn("Took CPLEX default branch at node "+ getNodeId());
                }

            }
            
        }    
    }
    
    private HyperCubeFilterResult  getFilteredHypercubes (TreeMap<Double, List<HyperCube>> hyperCubeMap ,
            TreeMap<String, Boolean> thisNodesVarFixings) throws IloException{
        
        HyperCubeFilterResult result = new HyperCubeFilterResult();
        
        
        for (Entry <Double, List<HyperCube>> entry : hyperCubeMap.entrySet()){
            
            for (  HyperCube cube:entry.getValue()){
                
                //filter on every var fixing
                HyperCube filteredCube = cube;
                //keep track of how much the key changes. Note that the key can be size or objective value
                double increaseInObjValue = ZERO;
                int decreaseInSize = ZERO;
                
                for (Entry<String, Boolean>  fixing :thisNodesVarFixings.entrySet()){
                    filteredCube = filteredCube.filter( fixing.getKey(),   fixing.getValue()  );
                    if (null ==filteredCube) break;
                    
                    
                    //find the updated key value                    
                    if (HEURISTIC_ENUM.equals( BRANCHING_HEURISTIC_ENUM.STEPPED_OBJECTIVE)){
                        //find increase in objective value
                        if (filteredCube.isFilterResult_simplePassThrough){
                            //var was not in the hypercube
                            //in this case , best possible objective must reflect this variable fixing
                            double varObjCoeff= Driver.objectiveFunctionMap.get( fixing.getKey());
                             
                            if ( (varObjCoeff > ZERO) &&   fixing.getValue()  ) increaseInObjValue +=varObjCoeff;
                            if ( (varObjCoeff < ZERO) &&  !fixing.getValue()  ) increaseInObjValue -=varObjCoeff;
                        }else {
                            //var was removed from the hypercube
                            //in this case , best possible objective does not change because this
                            //var and fixing was already accounted for
                        }
                    }   else {
                        decreaseInSize += filteredCube.isFilterResult_simplePassThrough ? ZERO: ONE;
                    }
                    
                    //check if infeasibility detected
                    if (HEURISTIC_ENUM.equals( BRANCHING_HEURISTIC_ENUM.STEPPED_OBJECTIVE)){
                       result.isNodeInfeasibilityDetected =(filteredCube.getSize()==ZERO);
                    }else {
                       //faster than checking size   
                       result.isNodeInfeasibilityDetected =(Math.round(entry.getKey()) ==decreaseInSize);
                    }
                    if( result.isNodeInfeasibilityDetected) {
                         
                        break;
                    }
                    
                }
                      
                //break for-loop of iteration over hypercubes at this level
                if (result.isNodeInfeasibilityDetected) break;
                
                //if filtered cube is not null, collect it
                //note that we do not do merge and absorb here, although we can (should?)
                if (null !=filteredCube ){    
                    Double newKey =  HEURISTIC_ENUM.equals( BRANCHING_HEURISTIC_ENUM.STEPPED_OBJECTIVE) ?
                            (entry.getKey() + increaseInObjValue): (entry.getKey()- decreaseInSize);
                    List<HyperCube> currentCubes = result.filteredHypercubes.get( newKey);
                    if (null==currentCubes) currentCubes = new ArrayList<HyperCube>();
                    currentCubes.add(filteredCube);
                    result.filteredHypercubes.put(newKey, currentCubes );                    
                }            
            }//iterate cubes at this level of infeasible hypercube map
            
            //no need to continue iteration over hypercube map
            if (result.isNodeInfeasibilityDetected) break;
            
        }//iteration over entire hypercube map
        
        return result;
    }
    
   
    
    private TreeMap<String, Boolean> getCplexFixedVars () throws IloException{
        TreeMap<String, Boolean>  fixedVars = new TreeMap<String, Boolean>  ();
        for (IloNumVar var : Driver.mapOfAllVariablesInTheModel.values()){
            Double upper = getUB(var);
            Double lower = getLB(var);
            if (  ZERO == Long.compare(Math.round(upper),Math.round(lower))){
                //value is false if 0 fixing
                fixedVars.put(var.getName(), ZERO!=   Long.compare( Math.round(getValue(var)), Math.round(ZERO))  );
            }
        } 
        return fixedVars;
    }
    
    
    private void getArraysNeededForCplexBranching (String branchingVar,IloNumVar[][] vars ,
                                                   double[ ][] bounds ,IloCplex.BranchDirection[ ][]  dirs ){
        
        IloNumVar branchingCplexVar = Driver.mapOfAllVariablesInTheModel.get(branchingVar );
        
        //get var with given name, and create up and down branch conditions
        vars[ZERO] = new IloNumVar[ONE];
        vars[ZERO][ZERO]= branchingCplexVar;
        bounds[ZERO]=new double[ONE ];
        bounds[ZERO][ZERO]=ZERO;
        dirs[ZERO]= new IloCplex.BranchDirection[ONE];
        dirs[ZERO][ZERO]=IloCplex.BranchDirection.Down;

        vars[ONE] = new IloNumVar[ONE];
        vars[ONE][ZERO]=branchingCplexVar;
        bounds[ONE]=new double[ONE ];
        bounds[ONE][ZERO]=ONE;
        dirs[ONE]= new IloCplex.BranchDirection[ONE];
        dirs[ONE][ZERO]=IloCplex.BranchDirection.Up;
    }
 

    /*private void printVarFixings (TreeMap<String, Boolean> varFixings) {
        logger.info("Printing Var Fixings " +varFixings.size() );
        for (Entry <String, Boolean> entry : varFixings.entrySet()){
            logger.info(entry.getKey() + " , " + entry.getValue());
        }
    }

    private void printInfeasibleHyperCubeMap( TreeMap<Double, List<HyperCube>>   hypercubes){
        logger.info("Printing Infeasible hypercubes " + hypercubes.size());
        for (Entry <Double, List<HyperCube>> entry : hypercubes.entrySet()){
            logger.info(entry.getKey() );
            for ( HyperCube cube: entry.getValue()){
                logger.info(cube.printMe() );
            }             
        }
    }*/
}
