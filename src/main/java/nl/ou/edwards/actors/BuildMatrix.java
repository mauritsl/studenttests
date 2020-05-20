package nl.ou.edwards.actors;

import java.util.LinkedList;
import java.util.List;

import akka.actor.AbstractActor;
import nl.ou.edwards.*;

/**
 * Build empty matrix using the available class implementations and test methods.
 */
public class BuildMatrix extends AbstractActor {
  
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
          
          List<Implementation> implementations = new LinkedList<Implementation>();
          List<Method> methods = new LinkedList<Method>();
          input.getImplementationNames().forEach(name -> {
            Implementation impl = input.getImplementation(name);
            implementations.add(impl);
            impl.getTestMethods().forEach(method -> {
              methods.add(method);
            });
          });
          
          Matrix matrix = new Matrix(implementations, methods);
          
          output.setMatrix(matrix);
          
          getSender().tell(output, getSelf());
        })
        .build();
  }
}
