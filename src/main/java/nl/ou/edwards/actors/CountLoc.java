package nl.ou.edwards.actors;

import akka.actor.AbstractActor;
import nl.ou.edwards.Implementation;
import nl.ou.edwards.Results;

/**
 * Add Lines of Code to implementations.
 */
public class CountLoc extends AbstractActor {
  
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
          
          // For each implementation, set the LOC (lines of code) in the results.
          output.getImplementationNames().forEach(name -> {
            Implementation impl = output.getImplementation(name);
            impl.setClassLoc(getLoc(impl.getClassCode()));
            impl.setTestLoc(getLoc(impl.getTestCode()));
          });
          
          getSender().tell(output, getSelf());
        })
        .build();
  }
  
  /**
   * Count number of lines in byte array.
   */
  private int getLoc(byte[] code) {
    int loc = 0;
    for (int i = 0; i < code.length; ++i) {
      if (code[i] == '\n') {
        ++loc;
      }
    }
    return loc;
  }
}
