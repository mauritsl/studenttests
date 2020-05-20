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
      .match(
        String.class,
        directory -> {
          ActorSelection actor = getContext().actorSelection("/user/ReadImplementations");
          actor.tell(new ReadImplementations.Input(directory), getSelf());
        }
      )
      .match(
        ReadImplementations.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/CountLoc");
          CountLoc.Input input = new CountLoc.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        CountLoc.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/ExtractTestMethods");
          ExtractTestMethods.Input input = new ExtractTestMethods.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        ExtractTestMethods.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/ExtractAssertions");
          ExtractAssertions.Input input = new ExtractAssertions.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        ExtractAssertions.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/FilterInvalidMethods");
          FilterInvalidMethods.Input input = new FilterInvalidMethods.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        FilterInvalidMethods.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/BuildMatrix");
          BuildMatrix.Input input = new BuildMatrix.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        BuildMatrix.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/TestMatrix");
          TestMatrix.Input input = new TestMatrix.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        TestMatrix.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/DeduplicateMatrix");
          DeduplicateMatrix.Input input = new DeduplicateMatrix.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        DeduplicateMatrix.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/CalculateBrc");
          CalculateBrc.Input input = new CalculateBrc.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        CalculateBrc.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/SearchSimilarMethods");
          SearchSimilarMethods.Input input = new SearchSimilarMethods.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        SearchSimilarMethods.Output.class,
        output -> {
          ActorSelection actor = getContext().actorSelection("/user/WriteExcelSheet");
          WriteExcelSheet.Input input = new WriteExcelSheet.Input();
          input.copyFrom(output);
          actor.tell(input, getSelf());
        }
      )
      .match(
        WriteExcelSheet.Output.class,
        output -> {
          getContext().getSystem().terminate();
        }
      )
      
      .build();
  }
}
