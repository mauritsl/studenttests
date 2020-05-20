package nl.ou.edwards;

import java.util.Set;

/**
 * Student implementation.
 */
public class Implementation {
  protected String name;
  protected byte[] classCode;
  protected byte[] testCode;
  protected int classLoc;
  protected int testLoc;
  protected Set<Method> helperMethods;
  protected Set<Method> testMethods;
  protected float bugRevealingCapacity;
  
  /**
   * Construct a new Implementation.
   */
  public Implementation(String name, byte[] classCode, byte[] testCode) {
    this.name = name;
    this.classCode = classCode;
    this.testCode = testCode;
  }
  
  /**
   * Get implementation name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get Java code for implementation class.
   */
  public byte[] getClassCode() {
    return classCode;
  }
  
  /**
   * Get Java code for test class.
   */
  public byte[] getTestCode() {
    return testCode;
  }
  
  /**
   * Get lines of code in class implementation.
   */
  public void setClassLoc(int l) {
    classLoc = l;
  }
  
  /**
   * Set number of lines in test suite.
   */
  public void setTestLoc(int l) {
    testLoc = l;
  }

  /**
   * Get lines of code in class implementation.
   */
  public int getClassLoc() {
    return classLoc;
  }

  /**
   * Get number of lines in test suite.
   */
  public int getTestLoc() {
    return testLoc;
  }

  /**
   * Set methods required for running the test methods.
   */
  public void setHelperMethods(Set<Method> helperMethods) {
    this.helperMethods = helperMethods;
  }
  
  /**
   * Set methods from test suite.
   */
  public void setTestMethods(Set<Method> testMethods) {
    this.testMethods = testMethods;
  }
  
  /**
   * Get methods required for running the test methods.
   */
  public Set<Method> getHelperMethods() {
    return helperMethods;
  }
  
  /**
   * Get methods from test suite.
   */
  public Set<Method> getTestMethods() {
    return testMethods;
  }
  
  /**
   * Set Bug Revealing Capacity.
   */
  public void setBugRevealingCapacity(float brc) {
    bugRevealingCapacity = brc;
  }

  /**
   * Get Bug Revealing Capacity.
   */
  public float getBugRevealingCapacity() {
    return bugRevealingCapacity;
  }
}
