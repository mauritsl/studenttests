package nl.ou.edwards;

import java.util.HashSet;
import java.util.Set;

/**
 * Representation of a class method.
 */
public class Method {
  protected Implementation source;
  protected String name;
  protected String declaration;
  protected String body;
  protected int variant;
  protected Set<Method> duplicates;
  protected Method similar;
  protected float similarity;
  
  /**
   * Construct a new Method.
   */
  public Method(Implementation source, String name, String declaration, String body) {
    this.source = source;
    this.name = name;
    this.declaration = declaration;
    this.body = body;
    this.duplicates = new HashSet<Method>();
  }
  
  /**
   * Get the implementation where this method belongs to.
   */
  public Implementation getSource() {
    return source;
  }
  
  /**
   * Get method name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get Java code for method declaration.
   */
  public String getDeclaration() {
    return declaration;
  }
  
  /**
   * Get Java code for method body.
   */
  public String getBody() {
    return body;
  }
  
  /**
   * Set variant number.
   */
  public void setVariant(int v) {
    variant = v;
  }
  
  /**
   * Get variant number.
   */
  public int getVariant() {
    return variant;
  }
  
  /**
   * Mark method as a duplicate.
   */
  public void addDuplicate(Method m) {
    duplicates.add(m);
  }
  
  /**
   * Get all methods marked as duplicate.
   */
  public Set<Method> getDuplicates() {
    return duplicates;
  }
  
  /**
   * Mark method as similar method.
   */
  public void setSimilarMethod(Method method, float similarity) {
    this.similar = method;
    this.similarity = similarity;
  }
  
  /**
   * Get similar method.
   */
  public Method getSimilarMethod() {
    return similar;
  }
  
  /**
   * Get similarity with similar method.
   * 
   * The similarity is measured as the percentage of
   * equal passes divided by the number of implementations.
   */
  public float getSimilarity() {
    return similarity;
  }
}