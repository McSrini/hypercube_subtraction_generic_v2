/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.common;
 
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.*;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Parameters.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author tamvadss
 */
public class LowerBoundConstraint {
        
    public List<VariableCoefficientTuple>   constraintExpression ;           
    public double lowerBound;     
    //public String name ;
    
    public LowerBoundConstraint( ){
        constraintExpression = new ArrayList<VariableCoefficientTuple>();
         
    }
    
    public void add (String var, Double coeff) {
        VariableCoefficientTuple tuple = new VariableCoefficientTuple (var, coeff);
        constraintExpression.add(tuple);
    }
    
    
    
    public LowerBoundConstraint( /*String name, */
                                 List<VariableCoefficientTuple>   constraint_Expr ,    
                                 double lowerBound ) {
        this.lowerBound =lowerBound;        
        constraintExpression =constraint_Expr ; //unsorted as of now
        
        //arrange variables in random order, this can be used to test performance variability because order of variables influences
        // the hypercube collection process .
        if (SHUFFLE_THE_CONSTRAINTS) Collections.shuffle(constraint_Expr,  PERF_VARIABILITY_RANDOM_GENERATOR);
        
        //this.name= name;
        
    }
    
    //shuffle and then sort
    public void shuffle() {
        Collections.shuffle(constraintExpression,  PERF_VARIABILITY_RANDOM_GENERATOR);
        //if (withSorting)  Collections.sort(constraintExpression );  
    }
    
    public void sort (){
        Collections.sort(constraintExpression );  
    }
    
    //fix all free vars at     values that will violate the constraint as far as possible
    public double getLargestPossibleInfeasibility() {
        double lhs = ZERO;
        for (VariableCoefficientTuple tuple : this.constraintExpression){
            if (tuple.coeff<ZERO){
                lhs+=tuple.coeff;
            }
        }
        
        double infeasiblity =lhs -this.lowerBound;
        //a 0 or positive value indicates that this constraint is trivially true, no matter what the variable choices
        return infeasiblity;
    }
        
    //reduced constraint is required during hypercube collection at the root
    public LowerBoundConstraint getReducedConstraint (Collection<String> zeroFixed, Collection<String> oneFixed) {
        double correctedLowerBound = lowerBound;
        List<VariableCoefficientTuple>  correctedTupleList = new ArrayList<VariableCoefficientTuple>   ();
         
        for (VariableCoefficientTuple tuple : this.constraintExpression){
            if (oneFixed.contains(tuple.varName) ){
                correctedLowerBound -=tuple.coeff;
            }else if (zeroFixed.contains(tuple.varName)){
                //do nothing
            }else {
                //add to expression
                correctedTupleList.add(tuple );
            }
        }
        
        //return new lbc, note variable sorting order stays the same ( no need to re-sort)
        return new LowerBoundConstraint (  /*name,*/ correctedTupleList ,   correctedLowerBound );
    }
        
     
    //toString() 
    public String printMe () {
        String result = " ";//+this.name+" ";
        for (VariableCoefficientTuple tuple:   this.constraintExpression){
            result +=tuple.coeff;
            result+="*";
            result+=tuple.varName;
            result+=" +";
        }
        
        return result+" >="+this.lowerBound;
    }
    
}
