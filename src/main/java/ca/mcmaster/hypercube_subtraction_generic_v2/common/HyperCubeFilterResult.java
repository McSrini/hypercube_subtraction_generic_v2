/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.hypercube_subtraction_generic_v2.common;

import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class HyperCubeFilterResult {
    public TreeMap<Double, List<HyperCube>>   filteredHypercubes=new TreeMap<Double, List<HyperCube>>  ();
    public boolean isNodeInfeasibilityDetected = false;
    
}
