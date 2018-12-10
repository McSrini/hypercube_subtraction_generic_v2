/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.common;

import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.ONE;
import static ca.mcmaster.hypercube_subtraction_generic_v2.Constants.ZERO;
import ca.mcmaster.hypercube_subtraction_generic_v2.Driver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class HyperCube {
     
    //we use treemaps to get sorted order of keys
    public TreeMap<String, Boolean> zeroFixingsMap = new  TreeMap<String, Boolean>();
    public TreeMap<String, Boolean> oneFixingsMap  = new  TreeMap<String, Boolean>();
    
    //flag used to discard merged and absorbed hypercubes
    public boolean isMarkedAsMerged = false;
    
    //result of filtering is stored here
    //set to true if fliter detected that var is not includd in this hyper cube
    public boolean isFilterResult_simplePassThrough = false;
    
        
    public HyperCube (Collection<String> zeroFixedvars ,Collection<String> oneFixedvars){
        for (String var: zeroFixedvars){
            zeroFixingsMap.put(var, false);  //value is ignored
        } 
        for (String var: oneFixedvars){
            oneFixingsMap.put(var, true); 
        }
         
    }
    
    //if failing filter, return null
    //if matching filter, return new hypercube with filter fixing removed
    //if no match, simply return self
    public HyperCube filter (String var, boolean isOneFixed) {
        HyperCube result = null;
        if (isOneFixed){
            //check if 1 fixing exists
            if (oneFixingsMap.containsKey(var)){
                //return new hypercube
                List<String> newOneFixedVars = new ArrayList<String>();
                newOneFixedVars.addAll(this.oneFixingsMap.keySet());
                newOneFixedVars.remove( var);
                
                result  = new HyperCube ( this.zeroFixingsMap.keySet(), newOneFixedVars) ;                      
                result.isFilterResult_simplePassThrough = false;
                
                
            } else if (zeroFixingsMap.containsKey(var)){
                //return null
            } else {
                //no fixing , pass thru
                result = this;
                result.isFilterResult_simplePassThrough = true;
            }
        }else {
            //check if 0 fixing exists
            if (zeroFixingsMap.containsKey(var)){
                //new hypercube
                List<String> newZeroFixedvars = new ArrayList<String>();
                newZeroFixedvars.addAll(this.zeroFixingsMap.keySet());
                newZeroFixedvars.remove(var );
                
                result  = new HyperCube (newZeroFixedvars,this.oneFixingsMap.keySet()) ;
                result.isFilterResult_simplePassThrough = false;                
                
                 
            } else if (oneFixingsMap.containsKey(var)){
                //null
            } else {
                //no fixing , pass thru
                result = this;
                result.isFilterResult_simplePassThrough = true;
            }
        }
        
        return result;
    }    
    
    public int getSize(){
        return  this.zeroFixingsMap.size() + this.oneFixingsMap.size();
    }
    public int getZeroFixingsSize(){
        return  this.zeroFixingsMap.size()  ;
    }
    public int getOneFixingsSize(){
        return   this.oneFixingsMap.size();
    }
    

    
    public boolean isDuplicate ( HyperCube other){
        boolean result = ( this.getZeroFixingsSize() ==other.getZeroFixingsSize())&&
                        ( this.getOneFixingsSize()==other.getOneFixingsSize()) ;
        return result && isAncestorOf (   other);
    }
     
    public boolean isAncestorOf ( HyperCube other){
       boolean result = ( this.getZeroFixingsSize() <=other.getZeroFixingsSize())&&
                        ( this.getOneFixingsSize()<=other.getOneFixingsSize()) ;
        
       
       if (result){
           int zeroSize = this.getZeroFixingsSize();
           String[] thisZeroFixings  = this.zeroFixingsMap .keySet().toArray(new String[ZERO]);
           String[] otherZeroFixings = other.zeroFixingsMap .keySet().toArray(new String[ZERO]);
           for (int index=ZERO; index < zeroSize; index++){
               if (! thisZeroFixings[index].equals(otherZeroFixings[index] )){
                   result = false;
                   break;
               }
           }
       }
       
       if (result){
           int oneSize = this.oneFixingsMap.size();
           String[] thisOneFixings = this.oneFixingsMap.keySet().toArray(new String[ZERO]);
           String[] otherOneFixings = other.oneFixingsMap.keySet().toArray(new String[ZERO]);
           for(int index=ZERO; index < oneSize; index++){
               if (! thisOneFixings[index].equals( otherOneFixings[index])){
                   result = false;
                   break;
               }
           }
       }
       
       
       return result;
    }
    
        
    public double getBestPossibleObjectiveValue ()        {
        double objectiveValueAtBestUnconstrainedVertex = ZERO;
        
        for (Map.Entry <String, Double> entry :  Driver.objectiveFunctionMap.entrySet()){
            String thisVar = entry.getKey() ;
            double coeff =entry.getValue();
            
            boolean isIncludedZero = this.zeroFixingsMap .containsKey(thisVar);
            boolean isIncludedOne = this.oneFixingsMap  .containsKey(thisVar );
            
            if ( isIncludedZero || isIncludedOne                ) {
                //already fixed 
                if (isIncludedOne) objectiveValueAtBestUnconstrainedVertex+= coeff;
            }else {
                //choose fixing so that objective becomes lowest possible
                if ( coeff < ZERO){
                     
                    objectiveValueAtBestUnconstrainedVertex+= coeff;
                }  
            }
        }
        
        
        return objectiveValueAtBestUnconstrainedVertex;
    }
    
    
    //merge two siblings, if other is a sibling
    //if other is not a sibling, then return null
    public HyperCube merge (HyperCube other) {
        HyperCube result = null;
        
        //all vars same, except 1 var complimentary
        int myZeroSize = this.zeroFixingsMap .size();
        int myOneSize = this.oneFixingsMap .size();
        int otherZeroSize = other.zeroFixingsMap .size();
        int otherOneSize = other.oneFixingsMap .size();
        
        boolean isSizeMatchOne = ( (myZeroSize == otherZeroSize-ONE) &&(myOneSize==otherOneSize+ONE)) ;
        boolean isSizeMatchTwo =   ( (myZeroSize == otherZeroSize+ONE) &&(myOneSize==otherOneSize-ONE)) ;
        
        boolean isComplimentary = true;
        
        if ( isSizeMatchOne) {
            List < String> extraZeroVar = new ArrayList<String> ();
            for (String var : other.zeroFixingsMap.keySet()) {
                if ( null==this.zeroFixingsMap.get(var)) extraZeroVar.add(var);
                if (extraZeroVar.size()>ONE){
                    //not complimentary
                    isComplimentary= false;
                    break;
                }
            }
            
            List < String> extraOneVar = new ArrayList<String> ();
            if (isComplimentary) {
                for (String var : this.oneFixingsMap.keySet()  ) {
                    if ( null==other.oneFixingsMap.get(var)) extraOneVar.add(var);
                    if (extraOneVar.size()>ONE){
                        //not complimentary
                        isComplimentary= false;
                        break;
                    }
                }
            }
            
            if (isComplimentary && extraOneVar.get(ZERO).equals(extraZeroVar.get(ZERO))) {
                //well and truly complimentary
                result = new HyperCube (this.zeroFixingsMap.keySet(),other.oneFixingsMap.keySet()) ;
            }  
        }else if (isSizeMatchTwo){
            List < String> extraZeroVar = new ArrayList<String> ();
            for (String var : this.zeroFixingsMap.keySet()) {
                if ( null==other.zeroFixingsMap.get(var)) extraZeroVar.add(var);
                if (extraZeroVar.size()>ONE){
                    //not complimentary
                    isComplimentary= false;
                    break;
                }
            }
            
            List < String> extraOneVar = new ArrayList<String> ();
            if (isComplimentary) {
                for (String var : other.oneFixingsMap.keySet()  ) {
                    if ( null==this.oneFixingsMap.get(var)) extraOneVar.add(var);
                    if (extraOneVar.size()>ONE){
                        //not complimentary
                        isComplimentary= false;
                        break;
                    }
                }
            }
            
            if (isComplimentary && extraOneVar.get(ZERO).equals(extraZeroVar.get(ZERO))) {
                //well and truly complimentary
                result = new HyperCube (other.zeroFixingsMap.keySet(),this.oneFixingsMap.keySet()) ;
            }
        }
        
        
        return result;
    }
    
    
        
    //toString() 
    public String printMe () {
        String result = " ZERO ";
        for (String var:   this.zeroFixingsMap.keySet()){
            result +=var +" ";
        }
        
        result = result + " ONE ";
        for (String var:   this.oneFixingsMap.keySet()){
            result +=var +" ";
        }
         
        result += " "+ this.isMarkedAsMerged;
        System.out.println(result) ;
        return result ;
    }
}
