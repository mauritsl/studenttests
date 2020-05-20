package nl.ou.edwards.actors;

import akka.actor.AbstractActor;
import nl.ou.edwards.Matrix;
import nl.ou.edwards.Results;

/**
 * Search for similar methods.
 */
public class SearchSimilarMethods extends AbstractActor {
  
  public static final class Input extends Results { }
  public static final class Output extends Results { }
  
  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(
        Input.class,
        input -> {
          Output output = new Output();
          output.copyFrom(input);
          
          Matrix m = output.getMatrix();
          int implementationsCount = m.getImplementations().size();
          int testMethodCount = m.getTestMethods().size();
          
          // For each combinations of two test methods...
          for (int i1 = 0; i1 < testMethodCount; ++i1) {
            int bestTestsEqual = 0;
            for (int i2 = 0; i2 < testMethodCount; ++i2) {
              int testsEqual = 0;
              // Loop through all implementations to get number of equal test results.
              for (int j = 0; j < implementationsCount; ++j) {
                if (m.getPass(j, i1) == m.getPass(j, i2)) {
                  testsEqual++;
                }
              }
              // Set as similar method when this is the best match found so far.
              if (testsEqual > bestTestsEqual && i1 != i2) {
                bestTestsEqual = testsEqual;
                m.getTestMethods().get(i1).setSimilarMethod(m.getTestMethods().get(i2), (float) testsEqual / implementationsCount);
              }
            }
          }
          
          getSender().tell(output, getSelf());
        })
        .build();
  }
}
