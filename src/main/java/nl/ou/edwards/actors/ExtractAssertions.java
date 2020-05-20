package nl.ou.edwards.actors;

import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import nl.ou.edwards.Implementation;
import nl.ou.edwards.Method;
import nl.ou.edwards.Results;

/**
 * Split test methods in variants per assertion.
 */
public class ExtractAssertions extends AbstractActor {
  
  public static final class Input extends Results { }
  public static final class Output extends Results { }
  
  /**
   * JavaParser visitor implementation to extract assertions.
   */
  private static class AssertionsExtractor extends ModifierVisitor<Void> {
    protected int keepAssertion;
    protected int assertionCount;
    protected String body;
    
    AssertionsExtractor(int keepAssertion) {
      this.keepAssertion = keepAssertion;
      assertionCount = 0;
    }
    
    AssertionsExtractor() {
      keepAssertion = -1;
      assertionCount = 0;
    }
    
    public int getAssertionCount() {
      return assertionCount;
    }
    
    public String getBody() {
      return body;
    }
    
    @Override
    public MethodCallExpr visit(MethodCallExpr mc, Void arg) {
      super.visit(mc, arg);
      
      String methodName = mc.getNameAsString();
      
      if (methodName.startsWith("assert")) {
        assertionCount++;
        if (keepAssertion >= 0 && keepAssertion != assertionCount - 1) {
          return null;
        }
      }
      
      return mc;
    }
    
    @Override
    public MethodDeclaration visit(MethodDeclaration md, Void arg) {
      super.visit(md, arg);
      body = md.getBody().get().toString();
      return md;
    }
  }
  
  /**
   * Get number of assertions for given method.
   */
  private int getAssertionCount(Method testMethod) throws Exception {
    AssertionsExtractor extractor = new AssertionsExtractor();
    String code = "class Test { public void main() " + testMethod.getBody() + "}";
    CompilationUnit unit = StaticJavaParser.parse(code);
    extractor.visit(unit, null);
    return extractor.getAssertionCount();
  }

  /**
   * Get Java code for given assertion.
   */
  private String getAssertionCode(Method testMethod, int assertionIndex) throws Exception {
    AssertionsExtractor extractor = new AssertionsExtractor(assertionIndex);
    String code = "class Test { public void main() " + testMethod.getBody() + "}";
    CompilationUnit unit = StaticJavaParser.parse(code);
    extractor.visit(unit, null);
    String classCode = unit.toString();
    
    extractor = new AssertionsExtractor();
    unit = StaticJavaParser.parse(classCode);
    extractor.visit(unit, null);
    return extractor.getBody();
  }
  
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  
  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(
        Input.class,
        input -> {
          Output output = new Output();
          output.copyFrom(input);
          output.clearImplementations();
          
          input.getImplementationNames().forEach(name -> {
            Implementation impl = input.getImplementation(name);
            
            // Build a new set of test methods for this implementation.
            Set<Method> testMethods = new HashSet<Method>();

            impl.getTestMethods().forEach(testMethod -> {
              try {
                int assertionCount = getAssertionCount(testMethod);
                for (int i = 0; i < assertionCount; ++i) {
                  String code = getAssertionCode(testMethod, i);
                  Method method = new Method(impl, testMethod.getName(), testMethod.getDeclaration(), code);
                  method.setVariant(i);
                  testMethods.add(method);
                }
              } catch (Exception e) {
                e.printStackTrace();
                log.info("Parse error: {} of {}", testMethod.getName(), impl.getName());
              }
            });
            
            // TODO: Clone implementation.
            Implementation assertImpl = impl;
            assertImpl.setTestMethods(testMethods);
            output.addImplementation(assertImpl);
            
          });
          
          getSender().tell(output, getSelf());
        })
        .build();
  }
}
