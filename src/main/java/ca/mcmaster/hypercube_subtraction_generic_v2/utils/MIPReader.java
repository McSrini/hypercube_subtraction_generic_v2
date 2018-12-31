/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.utils;
 
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.*;
import ca.mcmaster.hypercube_subtraction_generic_v2.common.*;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloObjectiveSense;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class MIPReader {
    
    //get all constraints as lower bounds
    //Improved method that does not use iterators
    public static List<LowerBoundConstraint> getConstraintsFast(IloCplex cplex) throws IloException{
        
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        
        final int numConstraints = lpMatrix.getNrows();
        final int numVariables = lpMatrix.getNcols();
        
        List<LowerBoundConstraint> result = new ArrayList<LowerBoundConstraint>( );
        
        
        int[][] ind = new int[ numConstraints][];
        double[][] val = new double[ numConstraints][];
        
        double[] lb = new double[numConstraints] ;
        double[] ub = new double[numConstraints] ;
        
        lpMatrix.getRows(ZERO,   numConstraints, lb, ub, ind, val);
        
        //build up each constraint 
        for (int index=ZERO; index < numConstraints ; index ++ ){
            
            //System.out.println(index);//k
                       
            boolean isUpperBound = Math.abs(ub[index])< BILLION ;
            boolean isLowerBound = Math.abs(lb[index])<BILLION ;
            boolean isEquality = ub[index]==lb[index];
            
            if (isEquality)  {
                LowerBoundConstraint lbcUP =new LowerBoundConstraint();
                LowerBoundConstraint lbcDOWN =new LowerBoundConstraint();
                 
                lbcUP .lowerBound= lb[index];
                lbcDOWN.lowerBound=-ub[index]; //ub portion
                
                for (  int varIndex = ZERO;varIndex< ind[index].length;   varIndex ++ ){
                    String var = lpMatrix.getNumVar(ind[index][varIndex]).getName() ;
                    Double coeff = val[index][varIndex];
                    lbcUP.add(var,  coeff) ;
                    lbcDOWN.add(var, -coeff);
                }
                
                
                result.add(lbcUP) ;
                //System.out.println(lbcUP.printMe());//k
                result.add(lbcDOWN) ;
                //System.out.println(lbcDOWN.printMe());//k
            }else {
                LowerBoundConstraint lbc =new LowerBoundConstraint();
                lbc.lowerBound=  (isUpperBound && ! isLowerBound )? -ub[index] : lb[index];
                for (  int varIndex = ZERO;varIndex< ind[index].length;   varIndex ++ ){
                    String var = lpMatrix.getNumVar(ind[index][varIndex]).getName() ;
                    Double coeff = val[index][varIndex];
                    lbc.add(var, (isUpperBound && ! isLowerBound )? -coeff: coeff) ;
                }
                result.add(lbc) ;
                //System.out.println(lbc.printMe());//k
            }
            
        }
 
        return result;
        
    }
    
    
    //get all constraints as lower bounds
    public static List<LowerBoundConstraint> getConstraints(IloCplex cplex) throws IloException{
        
        List<LowerBoundConstraint> result = new ArrayList<LowerBoundConstraint>();
        
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
         
          
        for (IloRange rangeConstraint : lpMatrix.getRanges()){    
            
           
           
            boolean isUpperBound = Math.abs(rangeConstraint.getUB())<Double.MAX_VALUE ;
            boolean isLowerBound = Math.abs(rangeConstraint.getLB())<Double.MAX_VALUE ;
            boolean isEquality = rangeConstraint.getUB()==rangeConstraint.getLB();
            boolean isRange = isUpperBound && isLowerBound && !isEquality;
            boolean isUpperBoundOnly =  isUpperBound && !isLowerBound  ;
            boolean isLowerBoundOnly =!isUpperBound && isLowerBound ;
            //equality constraints will be converted into 2 UB constraints - not handled right now
                        
            LowerBoundConstraint lbc = null;
            if ( isUpperBoundOnly || isLowerBoundOnly ) {
                
                //convert upper bound to lower bound  
                double pseudoBound = isUpperBound? -rangeConstraint.getUB(): rangeConstraint.getLB();
                IloLinearNumExprIterator constraintIterator =    ((IloLinearNumExpr) rangeConstraint.getExpr()).linearIterator();
                //this will be our representation of this constarint
                List<VariableCoefficientTuple>   constraintExpr = new ArrayList<VariableCoefficientTuple> ();
                while (constraintIterator.hasNext()) {
                    String varName = constraintIterator.nextNumVar().getName();
                    Double coeff =  constraintIterator.getValue();                    
                    constraintExpr.add(new VariableCoefficientTuple(varName, isUpperBound? -coeff: coeff));
                }
                
                //here is the constraint, in our format
                
                lbc  = new LowerBoundConstraint (/*rangeConstraint.getName(),*/   constraintExpr,   pseudoBound ) ;
                //add it to our list of constraints
                result.add(lbc);
                
                //System.out.println(lbc.printMe());
                //if (result.size()%HUNDRED ==ZERO) System.out.println(result.size());
                //logger.debug(lbc);
                
            }    else     if (isEquality) {
                     
                //we will add two constraints , one LB and one UB           
                IloLinearNumExprIterator constraintIterator =    ((IloLinearNumExpr) rangeConstraint.getExpr()).linearIterator();
                //this will be our representation of this constarint
                List<VariableCoefficientTuple>   constraintExprUB = new ArrayList<VariableCoefficientTuple> ();
                List<VariableCoefficientTuple>   constraintExprLB = new ArrayList<VariableCoefficientTuple> ();
                while (constraintIterator.hasNext()) {
                    String varName = constraintIterator.nextNumVar().getName();
                    Double coeff =  constraintIterator.getValue();
                    constraintExprLB.add(new VariableCoefficientTuple(varName, coeff));
                    constraintExprUB.add(new VariableCoefficientTuple(varName,   -coeff));
                }
                                
                //here is the LB constraint, in our format
                lbc  = new LowerBoundConstraint (/*rangeConstraint.getName()+NAME_FOR_EQUALITY_CONSTRAINT_LOWER_BOUND_PORTION,*/
                                                 constraintExprLB,   rangeConstraint.getLB() ) ;
                //add it to our list of constraints
                result.add(lbc);  
                 
                
                //logger.debug(lbc);
                //second constraint which is UB
                lbc  = new LowerBoundConstraint (/*rangeConstraint.getName()+NAME_FOR_EQUALITY_CONSTRAINT_UPPER_BOUND_PORTION,*/
                                                 constraintExprUB,  - rangeConstraint.getUB() ) ;
                //add it to our list of constraints
                result.add(lbc); 
                 
                          
            } else if (isUpperBound && isLowerBound && !isEquality) {
                System.err.println("Range constraints not allowed right now - LATER ");
                exit(ONE);
                 
            }
                   
        }//end for
        
        return result;
        
    }//end method getconstraints
       
   
    public static List<IloNumVar> getVariables (IloCplex cplex) throws IloException{
        List<IloNumVar> result = new ArrayList<IloNumVar>();
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        IloNumVar[] variables  =lpMatrix.getNumVars();
        for (IloNumVar var :variables){
            result.add(var ) ;
        }
        return result;
    }
    
    //minimization objective
    public static Map<String, Double> getObjective (IloCplex cplex) throws IloException {
        
        Map<String, Double>  objectiveMap = new HashMap<String, Double>();
        
        IloObjective  obj = cplex.getObjective();
        boolean isMaximization = obj.getSense().equals(IloObjectiveSense.Maximize);
        
        IloLinearNumExpr expr = (IloLinearNumExpr) obj.getExpr();
                 
        IloLinearNumExprIterator iter = expr.linearIterator();
        while (iter.hasNext()) {
           IloNumVar var = iter.nextNumVar();
           double val = iter.getValue();
           
           //convert  maximization to minimization 
            
           objectiveMap.put(var.getName(), !isMaximization ? val : -val );
        }
        
        return  objectiveMap ;
        
         
    }
}
