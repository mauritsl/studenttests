package nl.ou.edwards.actors;

import akka.actor.AbstractActor;
import nl.ou.edwards.Implementation;
import nl.ou.edwards.Matrix;
import nl.ou.edwards.Results;

/**
 * Add the Bug Revealing Capacity to implementations.
 */
public class CalculateBrc extends AbstractActor {
  
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
          
          // Get the observable failures from the deduplicated matrix.
          int observable = countFailures(input.getMatrix());
          
          // Loop through all implementations.
          output.getImplementationNames().forEach(name -> {
            Implementation impl = output.getImplementation(name);
            int observed = countFailures(input.getFullMatrix().filter(impl).deduplicate());

            // Calculate bug revealing capacity, avoiding division by zero.
            float brc = observable == 0 ? 0 : (float) observed / observable;
            impl.setBugRevealingCapacity(brc);
          });
          
          getSender().tell(output, getSelf());
        })
        .build();
  }
 
  /**
   * Count failures in a given matrix.
   */
  private int countFailures(Matrix m) {
    int count = 0;
    for (int i = 0; i < m.getTestMethods().size(); ++i) {
      for (int j = 0; j < m.getImplementations().size(); ++j) {
        if (!m.getPass(j, i)) {
          ++count;
        }
      }
    }
    return count;
  }
}
