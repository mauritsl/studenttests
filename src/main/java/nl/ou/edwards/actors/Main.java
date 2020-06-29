package nl.ou.edwards.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;

/**
 * Main application flow.
 */
public class Main extends AbstractActor {
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        
      // The flow starts when it recieves a string, which is the directory with all implementations.
      .match(
        String.class,
        directory -> {
          ActorSelection actor = getContext().actorSelection("/user/ReadImplementations");
          actor.tell(new ReadImplementations.Input(directory), getSelf());
        }
      )
      
      // Forward output from ReadImplementations to CountLoc.
      .match(
        ReadImplementations.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/CountLoc");
          CountLoc.Input input = new CountLoc.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from CountLoc to ExtractTestMethods.
      .match(
        CountLoc.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/ExtractTestMethods");
          ExtractTestMethods.Input input = new ExtractTestMethods.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )

      // Forward output from ExtractTestMethods to ExtractAssertions.
      .match(
        ExtractTestMethods.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/ExtractAssertions");
          ExtractAssertions.Input input = new ExtractAssertions.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from ExtractAssertions to FilterInvalidMethods.
      .match(
        ExtractAssertions.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/FilterInvalidMethods");
          FilterInvalidMethods.Input input = new FilterInvalidMethods.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from FilterInvalidMethods to BuildMatrix.
      .match(
        FilterInvalidMethods.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/BuildMatrix");
          BuildMatrix.Input input = new BuildMatrix.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from BuildMatrix to TestMatrix.
      .match(
        BuildMatrix.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/TestMatrix");
          TestMatrix.Input input = new TestMatrix.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from TestMatrix to DeduplicateMatrix.
      .match(
        TestMatrix.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/DeduplicateMatrix");
          DeduplicateMatrix.Input input = new DeduplicateMatrix.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from DeduplicateMatrix to CalculateBrc.
      .match(
        DeduplicateMatrix.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/CalculateBrc");
          CalculateBrc.Input input = new CalculateBrc.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from CalculateBrc to SearchSimilarMethods.
      .match(
        CalculateBrc.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/SearchSimilarMethods");
          SearchSimilarMethods.Input input = new SearchSimilarMethods.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Forward output from SearchSimilarMethods to WriteExcelSheet.
      .match(
        SearchSimilarMethods.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/WriteExcelSheet");
          WriteExcelSheet.Input input = new WriteExcelSheet.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      
      // Terminate after WriteExcelSheet.
      .match(
        WriteExcelSheet.Output.class,
        output -> {
          getContext().getSystem().terminate();
        }
      )
      
      .build();
  }
}
