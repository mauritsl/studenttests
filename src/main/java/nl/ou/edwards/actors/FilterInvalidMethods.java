package nl.ou.edwards.actors;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import nl.ou.edwards.*;

/**
 * Validate test methods against the reference implementation and filter methods that do not pass.
 */
public class FilterInvalidMethods extends AbstractActor {
  
  public static final class Input extends Results { }
  public static final class Output extends Results { }
  public static final class Attachment {
    private Results results;
    private List<Method> testMethods;
    private ActorRef sender;
    
    public Attachment(Results results, List<Method> testMethods, ActorRef sender) {
      this.results = results;
      this.testMethods = testMethods;
      this.sender = sender;
    }
    
    public Results getResults() {
      return results;
    }
    
    public List<Method> getTestMethods() {
      return testMethods;
    }
    
    public ActorRef getSender() {
      return sender;
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
          
          // Compose a list of all test methods, across all implementations.
          List<Method> testMethods = new LinkedList<Method>();
          output.getImplementationNames().forEach(name -> {
            // Assume that the reference test passes the reference implementation.
            if (!name.equals("_reference")) {
              Implementation impl = output.getImplementation(name);
              impl.getTestMethods().forEach(method -> {
                testMethods.add(method);
              });
            }
          });
          
          Attachment attachment = new Attachment(output, testMethods, getSender());
          handleInput(attachment);
        }
      )
      .match(
        RunTest.Output.class,
        testOutput -> {
          try {
            Attachment attachment = (Attachment) testOutput.getAttachment();
            
            // Shift off the first method from the list.
            Method method = attachment.getTestMethods().get(0);
            attachment.getTestMethods().remove(0);
            
            if (!testOutput.getPassed()) {
              // This method failed the test. Remove it from the test set.
              Implementation impl = method.getSource();
              impl.getTestMethods().remove(method);
              
              log.info("Method failed on reference test: {} from {}, variant {}", method.getName(), method.getSource().getName(), method.getVariant());
            }
            
            handleInput(attachment);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      )
      .build();
  }
  
  /**
   * Process results that comes back from the RunTest actor.
   */
  private void handleInput(Attachment attachment) {
    Results input = attachment.getResults();
    
    int methodsToTest = attachment.getTestMethods().size();
    
    if (attachment.getResults().getImplementation("_reference") == null) {
      log.warning("Reference implementation not found, skipping invalid methods test");
      methodsToTest = 0;
    }
    
    if (methodsToTest > 0) {
      // We still have methods to test.
      Method method = attachment.getTestMethods().get(0);
      Implementation reference = attachment.getResults().getImplementation("_reference");
      
      ActorSelection actor = getContext().actorSelection("/user/RunTest");
      actor.tell(new RunTest.Input<Attachment>(
        new String(reference.getClassCode(), StandardCharsets.UTF_8),
        method,
        method.getSource().getHelperMethods(),
        attachment
      ), getSelf());
      
      log.info("Testing {} ({} methods left)", method.getName(), methodsToTest);
      
    } else {
      // All methods are tested. Send results back to original sender.
      Output output = new Output();
      output.copyFrom(input);
      attachment.getSender().tell(output, getSelf());
    }
    
  }
}
