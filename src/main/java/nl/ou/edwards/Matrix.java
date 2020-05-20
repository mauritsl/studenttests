package nl.ou.edwards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Matrix that stores test results of implementation and test method combinations.
 */
public class Matrix {
  protected List<Implementation> implementations;
  protected List<Method> testMethods;
  protected ArrayList<Boolean> data;
  protected int testedMethodCount;

  /**
   * Create a new Matrix.
   */
  public Matrix(List<Implementation> implementations, List<Method> testMethods) {
    this.implementations = implementations;
    this.testMethods = testMethods;
    this.data = new ArrayList<Boolean>(Collections.nCopies(implementations.size() * testMethods.size(), false));
    this.testedMethodCount = 0;
  }
  
  /**
   * Get all implementations, in the same order as the matrix data.
   */
  public List<Implementation> getImplementations() {
    return implementations;
  }

  /**
   * Get all test methods, in the same order as the matrix data.
   */
  public List<Method> getTestMethods() {
    return testMethods;
  }
  
  /**
   * Set a single result.
   */
  public void setPass(int implementation, int method, boolean pass) {
    int rowSize = implementations.size();
    int index = method * rowSize + implementation;
    if (data.get(index) == null) {
      testedMethodCount++;
    }
    data.set(index, pass);
  }

  /**
   * Set the result for the first implementation / test method combination not yet filled in.
   * 
   * The purpose of this method is to provide an easy way to sequentially test
   * all combinations in the matrix in the TestMatrix actor.
   */
  public void setNextPass(boolean pass) {
    // @TODO Test for index out-of-bounds.
    data.set(this.getTestedMethodCount(), pass);
    testedMethodCount++;
  }
  
  /**
   * Get the number of tested methods.
   */
  public int getTestedMethodCount() {
    return testedMethodCount;
  }

  /**
   * Get the test result for a specific implementation / test method combination.
   */
  public boolean getPass(int implementation, int method) {
    int rowSize = implementations.size();
    return data.get(method * rowSize + implementation);
  }
  
  /**
   * Get the size of the matrix, calculated as the number of implementation times test methods.
   */
  public int getSize() {
    return implementations.size() * testMethods.size();
  }
  
  /**
   * Get a deduplicated variant of this matrix.
   */
  public Matrix deduplicate() {
    Map<String,Method> lookupMap = new TreeMap<String,Method>();
    List<Method> uniqueMethods = new LinkedList<Method>();
    
    for (int i = 0; i < getTestMethods().size(); ++i) {
      Method method = getTestMethods().get(i);
      
      // Build a signature that uniquely identify the results for this row.
      String signature = "";
      for (int j = 0; j < getImplementations().size(); ++j) {
        signature = signature + (getPass(j, i) ? "1" : "0");
      }
      
      // Check if this is a duplicate.
      if (lookupMap.containsKey(signature)) {
        lookupMap.get(signature).addDuplicate(method);
      } else {
        lookupMap.put(signature, method);
        uniqueMethods.add(method);
      }
    }
    
    return filter(uniqueMethods);
  }
  
  /**
   * Get a variant filtered by implementation.
   */
  public Matrix filter(Implementation impl) {
    List<Method> implementationMethods = new LinkedList<Method>();
    
    for (int i = 0; i < getTestMethods().size(); ++i) {
      Method method = getTestMethods().get(i);
      if (method.getSource() == impl) {
        implementationMethods.add(method);
      }
    }
    
    return filter(implementationMethods);
  }
  
  /**
   * Get a variant filtered by selected test methods.
   */
  public Matrix filter(List<Method> methods) {
    Matrix output = new Matrix(getImplementations(), methods);
    for (int i = 0; i < output.getTestMethods().size(); ++i) {
      for (int j = 0; j < output.getImplementations().size(); ++j) {
        int fullIndex = getTestMethods().indexOf(output.getTestMethods().get(i));
        output.setPass(j, i, getPass(j, fullIndex));
      }
    }
    return output;
  }
  
}
