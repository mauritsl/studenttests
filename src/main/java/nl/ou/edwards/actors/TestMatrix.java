package nl.ou.edwards.actors;

import java.nio.charset.StandardCharsets;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;

import nl.ou.edwards.*;

/**
 * Run tests for all class implementation / test method combinations in the matrix.
 */
public class TestMatrix extends AbstractActor {
  
  public static final class Input extends Results { }
  public static final class Output extends Results { }
  
  @Override
  public Receive createReceive() {
    
    return receiveBuilder()
        
      // When receiving Input (likely from the Main actor), call testMatrix(). 
      .match(
        Input.class,
        input -> {
          testMatrix(input);
        }
      )
      
      // When receiving the results, get its attachment and proceed using testMatrix().
      .match(
        RunTest.Output.class,
        testOutput -> {
          Input input = (Input) testOutput.getAttachment();
          input.getMatrix().setNextPass(testOutput.getPassed());
          testMatrix(input);
          
        }
      )
      .build();
  }
  
  /**
   * Test combinations in the matrix.
   * 
   * This method calls the RunTest method for only one combination at a time.
   * The RunTest actor returns the results to the sender (this actor), after which
   * this method is called again.
   * On each invocation, decide whether there is anything left to test.
   * If so, repeat the loop with the RunTest actor, or return the results to
   * the original sender (Main actor) when all combinations are tested.
   */
  private void testMatrix(Input input) {
    Matrix matrix = input.getMatrix();
    int testedMethodCount = matrix.getTestedMethodCount();
    
    // If there is anything left to test.
    if (testedMethodCount < matrix.getSize()) {
      System.out.println("Testing " + (testedMethodCount + 1) + "/" + matrix.getSize());
      
      int rowSize = matrix.getImplementations().size();
      int methodIndex = Math.floorDiv(testedMethodCount, rowSize);
      int implementationIndex = Math.floorMod(testedMethodCount, rowSize);
      
      // Get the test method and implementation for the next test.
      Method testMethod = matrix.getTestMethods().get(methodIndex);
      Implementation impl = matrix.getImplementations().get(implementationIndex);

      // Call the RunTest actor.
      ActorSelection actor = getContext().actorSelection("/user/RunTest");
      actor.tell(new RunTest.Input<Input>(
        new String(impl.getClassCode(), StandardCharsets.UTF_8),
        testMethod,
        testMethod.getSource().getHelperMethods(),
        input
      ), getSelf());
      
    } else {
      
      // Return results to the original sender.
      Output output = new Output();
      output.copyFrom(input);
      ActorSelection actor = getContext().actorSelection("/user/Main");
      actor.tell(output, getSelf());
    }
  }
}
