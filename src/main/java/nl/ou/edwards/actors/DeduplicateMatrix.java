package nl.ou.edwards.actors;

import akka.actor.AbstractActor;
import nl.ou.edwards.Matrix;
import nl.ou.edwards.Results;

/**
 * Add deduplicated matrix to results.
 */
public class DeduplicateMatrix extends AbstractActor {
  
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
          
          Matrix full = output.getMatrix();
          Matrix deduplicatedMatrix = full.deduplicate();
          
          output.setFullMatrix(full);
          output.setMatrix(deduplicatedMatrix);
          
          getSender().tell(output, getSelf());
        })
        .build();
  }

}
