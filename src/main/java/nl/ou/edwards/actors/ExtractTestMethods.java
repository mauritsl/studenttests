package nl.ou.edwards.actors;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import nl.ou.edwards.Implementation;
import nl.ou.edwards.Method;
import nl.ou.edwards.Results;

/**
 * Add test methods to results.
 */
public class ExtractTestMethods extends AbstractActor {
  
  public static final class Input extends Results { }
  public static final class Output extends Results { }
  
  private static class TestMethodsExtractor extends VoidVisitorAdapter<Void> {
    protected Implementation implementation;
    protected Set<Method> helperMethods;
    protected Set<Method> testMethods;
    
    TestMethodsExtractor(Implementation implementation) {
      this.implementation = implementation;
      
      helperMethods = new HashSet<Method>();
      testMethods = new HashSet<Method>();
    }
    
    public Set<Method> getHelperMethods() {
      return helperMethods;
    }
    
    public Set<Method> getTestMethods() {
      return testMethods;
    }
    
    @Override
    public void visit(MethodDeclaration md, Void arg) {
      super.visit(md, arg);
      
      String methodName = md.getName().toString();
      String body = md.getBody().get().toString();
      
      // When testing JUnit tests, consider checking md.getAnnotations() for the @Test annotation.
      // boolean isTestMethod = md.getTypeAsString().equals("void") && !md.getName().toString().equals("assertEquals");
      boolean isTestMethod = body.contains("assert");
      
      String declaration = md.getDeclarationAsString();
      Method method = new Method(implementation, methodName, declaration, body);
      if (isTestMethod) {
        testMethods.add(method);
      } else if (!methodName.equals("main")) {
        helperMethods.add(method);
      }
    }
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
          
          // Clear implementations. Re-add if they do not contain errors.
          output.clearImplementations();
          
          input.getImplementationNames().forEach(name -> {
            Implementation impl = input.getImplementation(name);
            
            String code = new String(impl.getTestCode(), StandardCharsets.UTF_8);
            
            TestMethodsExtractor extractor = new TestMethodsExtractor(impl);
            try {
              extractor.visit(StaticJavaParser.parse(code), null);
              
              impl.setTestMethods(extractor.getTestMethods());
              impl.setHelperMethods(extractor.getHelperMethods());
              output.addImplementation(impl);
            } catch (Exception e) {
              log.info("Parse error: {}", impl.getName());
            }
          });
          
          getSender().tell(output, getSelf());
        })
        .build();
  }
}
