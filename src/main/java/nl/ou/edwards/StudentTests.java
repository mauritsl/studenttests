package nl.ou.edwards;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import nl.ou.edwards.actors.*;

/**
 * Student Tests analysis tool.
 */
public class StudentTests {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: java -jar studenttests.jar /path/to/data /path/for/tests");
      return;
    }
    
    Config config = Config.getInstance();
    config.setDataDirectory(args[0]);
    config.setTestDirectory(args[1]);
    
    ActorSystem system = ActorSystem.create("studenttests");
    
    system.actorOf(Props.create(ReadImplementations.class), "ReadImplementations");
    system.actorOf(Props.create(CountLoc.class), "CountLoc");
    system.actorOf(Props.create(ExtractTestMethods.class), "ExtractTestMethods");
    system.actorOf(Props.create(ExtractAssertions.class), "ExtractAssertions");
    system.actorOf(Props.create(FilterInvalidMethods.class), "FilterInvalidMethods");
    system.actorOf(Props.create(BuildMatrix.class), "BuildMatrix");
    system.actorOf(Props.create(TestMatrix.class), "TestMatrix");
    system.actorOf(Props.create(DeduplicateMatrix.class), "DeduplicateMatrix");
    system.actorOf(Props.create(CalculateBrc.class), "CalculateBrc");
    system.actorOf(Props.create(SearchSimilarMethods.class), "SearchSimilarMethods");
    system.actorOf(Props.create(WriteExcelSheet.class), "WriteExcelSheet");
    
    system.actorOf(Props.create(RunTest.class), "RunTest");
    
    ActorRef main = system.actorOf(Props.create(Main.class), "Main");
    
    main.tell(config.getDataDirectory(), ActorRef.noSender());
  }
}
