/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.cplex;
 
import ca.mcmaster.hypercube_subtraction_generic_v2.common.HyperCube;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class NodePayload implements IloCplex.MIPCallback.NodeData  {
    
    public TreeMap<Double, List<HyperCube>>   infeasibleHypercubesMap =null;
    
    //false indicates 0 fixing
    public TreeMap<String, Boolean> parentVarFixings = new  TreeMap<String, Boolean>();
 
    public void delete() {
          
        //we expect java to garbage collect, we do not explicitly free anything
        
        /* if (infeasibleHypercubesMap!=null){
             infeasibleHypercubesMap.clear();
             infeasibleHypercubesMap=null;
         }
         parentVarFixings.clear();
         parentVarFixings=null;
        */
    }
    
}
