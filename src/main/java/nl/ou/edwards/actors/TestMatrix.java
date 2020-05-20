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
      .match(
        Input.class,
        input -> {
          testMatrix(input);
          
        }
      )
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
  
  private void testMatrix(Input input) {
    Matrix matrix = input.getMatrix();
    int testedMethodCount = matrix.getTestedMethodCount();
    
    if (testedMethodCount < matrix.getSize()) {
      System.out.println("Testing " + (testedMethodCount + 1) + "/" + matrix.getSize());
      
      int rowSize = matrix.getImplementations().size();
      int methodIndex = Math.floorDiv(testedMethodCount, rowSize);
      int implementationIndex = Math.floorMod(testedMethodCount, rowSize);
      
      Method testMethod = matrix.getTestMethods().get(methodIndex);
      Implementation impl = matrix.getImplementations().get(implementationIndex);
      
      ActorSelection actor = getContext().actorSelection("/user/RunTest");
      actor.tell(new RunTest.Input<Input>(
        new String(impl.getClassCode(), StandardCharsets.UTF_8),
        testMethod,
        testMethod.getSource().getHelperMethods(),
        input
      ), getSelf());
      
    } else {
      Output output = new Output();
      output.copyFrom(input);
      ActorSelection actor = getContext().actorSelection("/user/Main");
      actor.tell(output, getSelf());
    }
  }
}
