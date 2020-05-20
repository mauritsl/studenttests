package nl.ou.edwards;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Test results.
 * 
 * This class is used to package all test results in a single
 * object so we can send it between actors.
 */
public class Results {
  protected Map<String, Implementation> implementations;
  protected Matrix matrix;
  protected Matrix fullMatrix;
  
  /**
   * Create a new Results object.
   */
  public Results() {
    // Use a TreeMap to keep names sorted.
    implementations = new TreeMap<String, Implementation>();
  }
  
  /**
   * Add an implementation.
   * 
   * Note that this will not update matrices.
   */
  public void addImplementation(Implementation impl) {
    String name = impl.getName();
    implementations.put(name, impl);
  }

  /**
   * Remove all implementations.
   */
  public void clearImplementations() {
    implementations.clear();
  }

  /**
   * Get a list of implementation names.
   */
  public List<String> getImplementationNames() {
    List<String> names = new LinkedList<String>();
    implementations.forEach((key, entry) -> {
      names.add(key);
    });
    return names;
  }
  
  /**
   * Get implementation by name.
   */
  public Implementation getImplementation(String name) {
    return implementations.get(name);
  }

  /**
   * Set full matrix.
   */
  public void setMatrix(Matrix m) {
    matrix = m;
  }

  /**
   * Get full matrix.
   */
  public Matrix getMatrix() {
    return matrix;
  }

  /**
   * Copy data from other Results object.
   */
  public void copyFrom(Results source) {
    implementations = new TreeMap<String, Implementation>();
    source.getImplementationNames().forEach(name -> {
      addImplementation(source.getImplementation(name));
    });
    matrix = source.getMatrix();
    fullMatrix = source.getFullMatrix();
  }

  /**
   * Get all implementations, keyed by name.
   */
  public Map<String, Implementation> getImplementations() {
    return implementations;
  }

  /**
   * Set full matrix.
   */
  public void setFullMatrix(Matrix m) {
    this.fullMatrix = m;
  }

  /**
   * Get full matrix.
   */
  public Matrix getFullMatrix() {
    return fullMatrix;
  }
  
}
