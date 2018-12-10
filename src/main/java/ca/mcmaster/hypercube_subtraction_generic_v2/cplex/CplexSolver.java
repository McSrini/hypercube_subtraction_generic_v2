/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.cplex;

import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.*;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.SIXTY;
import static ca.mcmaster.hypercube_subtraction_generic_v2.CplexParameters.*;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Parameters.*;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.HyperCube;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import static ilog.cplex.IloCplex.IncumbentId;
import static ilog.cplex.IloCplex.Status.Infeasible;
import static ilog.cplex.IloCplex.Status.Optimal;
import java.io.File;
import static java.lang.System.exit;
import java.util.List;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class CplexSolver {
        
    private IloCplex cplex ;
    private IloCplex.BranchCallback branchingCallback = null;
    
    private static Logger logger=Logger.getLogger(CplexSolver.class);
    static {
        logger.setLevel(LOGGING_LEVEL );
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+CplexSolver.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
     
    public CplexSolver (       TreeMap<Double, List<HyperCube>>   infeasibleHypercubeMap ) throws IloException {
        cplex = new IloCplex() ;
        cplex.importModel(MIP_FILENAME);
        
        cplex.setParam(IloCplex.Param.Emphasis.MIP,  MIP_EMPHASIS);
        cplex.setParam( IloCplex.Param.Threads, MAX_THREADS);
        cplex.setParam(IloCplex.Param.MIP.Strategy.File,  FILE_STRATEGY);   
         
        if (DISABLE_HEURISTICS) cplex.setParam( IloCplex.Param.MIP.Strategy.HeuristicFreq , -ONE);
        if (DISABLE_PROBING) cplex.setParam(IloCplex.Param.MIP.Strategy.Probe, -ONE);
        
        if (DISABLE_PRESOLVENODE) cplex.setParam(IloCplex.Param.MIP.Strategy.PresolveNode, -ONE);
       
        if (DISABLE_PRESOLVE) cplex.setParam(IloCplex.Param.Preprocessing.Presolve, false);
        
        if (DISABLE_CUTS) cplex.setParam(IloCplex.Param.MIP.Limits.CutPasses, -ONE);
        
        
        if (USE_PURE_CPLEX){
            branchingCallback=new EmptyBranchHandler();
        }else {
            branchingCallback=new HypercubeBranchHandler( infeasibleHypercubeMap );
        }
        
    }
    
    
    public void solve (int rampUpDurationHours, int solveDurationHours  ) throws IloException{
        for (int hours=ZERO; hours < rampUpDurationHours; hours ++){
            
            if (isHaltFilePresent()) break;
            
            cplex.clearCallbacks();
            cplex.use( this.branchingCallback);
            cplex.setParam( IloCplex.Param.TimeLimit,  SIXTY*SIXTY);
            //switch to the correct number of threads
            cplex.setParam( IloCplex.Param.Threads, MAX_THREADS);
            cplex.solve();

            //print stats
            StaticticsCallback stats = new StaticticsCallback();
            cplex.use (stats) ; 
            //switch to 1 thread and solve
            cplex.setParam( IloCplex.Param.Threads, ONE);
            cplex.solve();
            logger.info (  " , " + hours + " , " + stats.bestKnownBound + " , "+ stats.bestKnownSOlution + " , "+
                            stats.numberOFLeafs +  " , "+ stats.numberOFNodesProcessed );
            
            //stop iterations if completely solved
            if (cplex.getStatus().equals(Optimal)||cplex.getStatus().equals(Infeasible)) break;
        }
        
        for (int hours=ZERO; hours < solveDurationHours; hours ++){
            
            if (isHaltFilePresent()) break;
            
            cplex.clearCallbacks();
            cplex.use(new EmptyBranchHandler());
            cplex.setParam( IloCplex.Param.TimeLimit,   SIXTY*SIXTY);
            //switch to the correct number of threads
            cplex.setParam( IloCplex.Param.Threads, MAX_THREADS);
            cplex.solve();

            //print stats
            StaticticsCallback stats = new StaticticsCallback();
            cplex.use (stats) ;
            //switch to 1 thread and solve
            cplex.setParam( IloCplex.Param.Threads, ONE);
            cplex.solve();
            logger.info (   " , " + (rampUpDurationHours+hours )+ " , " + stats.bestKnownBound + " , "+ stats.bestKnownSOlution + " , "+
                            stats.numberOFLeafs +  " , "+ stats.numberOFNodesProcessed );
            
            //stop iterations if completely solved
            if (cplex.getStatus().equals(Optimal)||cplex.getStatus().equals(Infeasible)) break;
            
        }
        
         
    }
    
    public void printSolution () throws IloException{
        if (cplex.getStatus().equals(IloCplex.Status.Optimal)|| cplex.getStatus().equals(IloCplex.Status.Feasible)){
            //print vector and value of incumbent
            printIncumbent() ;
        } else {
            logger.info ("status is " + cplex.getStatus());
            if (!cplex.getStatus().equals(IloCplex.Status.Infeasible) ) logger.info ( " and bound is "+ cplex.getBestObjValue()) ;
        }
    }
    
     
    
    private void printIncumbent() throws IloException {
        logger.info ("best known solution is " + cplex.getObjValue()) ;
        logger.info ("best known bound is " + cplex.getBestObjValue()) ;
        logger.info ("status is " + cplex.getStatus()) ;
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        for (IloNumVar var :  lpMatrix.getNumVars()) {            
             logger.info ("var is " + var.getName() + " and is soln value is " + cplex.getValue(var, IncumbentId )) ;
        }
    }
    
    private static boolean isHaltFilePresent (){
        File file = new File("haltfile.txt");         
        return file.exists();
    }
    
}
