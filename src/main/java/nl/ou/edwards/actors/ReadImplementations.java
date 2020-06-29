package nl.ou.edwards.actors;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import nl.ou.edwards.Implementation;
import nl.ou.edwards.Results;

/**
 * Read all implementations from data directory.
 */
public class ReadImplementations extends AbstractActor {
  
  public static final class Input {
    protected String directory;
    
    public Input(String directory) {
      this.directory = directory;
    }
    
    public String getDirectory() {
      return directory;
    }
  }
  
  public static final class Output extends Results { }
  
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  
  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(
        Input.class,
        input -> {
          Results results = new Output();
          
          // Read subfolders in submissions folder.
          File folder = new File(input.getDirectory());
          for (File submission : folder.listFiles()) {
            String submissionName = submission.getName();
            if (submission.isDirectory()) {
              
              // Look for class implementation and test suite.
              File classFile = null;
              File testFile = null;
              for (File submissionFile : submission.listFiles()) {
                if (submissionFile.isFile()) {
                   String name = submissionFile.getName();
                   if (name.equals("LinkedQueue.java")) {
                     classFile = submissionFile;
                   }
                   if (name.equals("QueueTest.java")) {
                     testFile = submissionFile;
                   }
                }
              }
              
              // If both found, read implementation files and add to our results.
              if (testFile != null && classFile != null) {
                byte[] classCode = Files.readAllBytes(Paths.get(classFile.getPath()));
                byte[] testCode = Files.readAllBytes(Paths.get(testFile.getPath()));
                Implementation impl = new Implementation(submissionName, classCode, testCode);
                
                results.addImplementation(impl);
              } else {
                log.info("Incomplete submission: {}", submissionName);
              }
            }
          }
          
          getSender().tell(results, getSelf());
          
        })
        .build();
  }
}
