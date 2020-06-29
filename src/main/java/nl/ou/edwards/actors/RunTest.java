package nl.ou.edwards.actors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import nl.ou.edwards.Config;
import nl.ou.edwards.Method;

/**
 * Run a single test.
 */
public class RunTest<A> extends AbstractActor {
  
  /**
   * Input definitions for RunTest actor.
   */
  public static final class Input<A> {
    protected String classCode;
    protected Method testMethod;
    protected Set<Method> helperMethods;
    protected A attachment;
    
    /**
     * Create a new Input object.
     */
    public Input(String classCode, Method testMethod, Set<Method> helperMethods, A attachment) {
      this.classCode = classCode;
      this.testMethod = testMethod;
      this.helperMethods = helperMethods;
      this.attachment = attachment;
    }

    /**
     * Get code for class implementation.
     */
    public String getClassCode() {
      return classCode;
    }

    /**
     * Get test method.
     */
    public Method getTestMethod() {
      return testMethod;
    }
    
    /**
     * Get helper methods.
     * 
     * Helper methods are required to run the test methods.
     */
    public Set<Method> getHelperMethods() {
      return helperMethods;
    }

    /**
     * Get attachment.
     * 
     * Calling actors can use an attachment to carry information they need after they get the results back.
     */
    public A getAttachment() {
      return attachment;
    }
  }
  
  /**
   * Output definitions for RunTest actor.
   */
  public static final class Output<A> {
    protected boolean passed;
    protected A attachment;
    
    /**
     * Create a new Output object.
     */
    public Output(boolean passed, A attachment) {
      this.passed = passed;
      this.attachment = attachment;
    }

    /**
     * Get test result.
     * 
     * Returns true if the test passed, false otherwise.
     */
    public boolean getPassed() {
      return passed;
    }
    
    /**
     * Get attachment.
     */
    public A getAttachment() {
      return attachment;
    }
  }
  
  /**
   * Read all data from a stream into a string.
   */
  private String readStream(InputStream is) {
    String out = "";
    try {
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String line=null;
      while ( (line = br.readLine()) != null) {
        out = out + line + "\n";
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();  
    }
    return out;
  }
  
  /**
   * Run command and return output.
   */
  private String run(String command) {
    String output = "";
    try {
      Runtime rt = Runtime.getRuntime();
      Process proc = rt.exec(command);
      boolean success = proc.waitFor(10, TimeUnit.SECONDS);
      if (success) {
        output = readStream(proc.getErrorStream());
        output = output + readStream(proc.getInputStream());
      } else {
        proc.destroyForcibly();
        output = "Exception: timeout";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return output;
  }
 
  @SuppressWarnings("unchecked")
  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(
        Input.class,
        input -> {
          try {
            // Get configured directories.
            String testDir = Config.getInstance().getTestDirectory();
            String javaHome = System.getenv("JAVA_HOME");
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(testDir + "/LinkedQueue.java"));
            writer.write(input.getClassCode());
            writer.close();
            
            String str = "import java.util.Iterator;\n" + 
                "\n" + 
                "public class QueueTest { public static void main(String[] args)"
                + input.getTestMethod().getBody();
            LinkedList<String> s = new LinkedList<String>();
            input.getHelperMethods().forEach(method -> {
              s.add(((Method) method).getDeclaration() + ((Method) method).getBody());
            });
            str = str + String.join("\n", s); 
            str = str + "}";
            
            writer = new BufferedWriter(new FileWriter(testDir + "/QueueTest.java"));
            writer.write(str);
            writer.close();
            
            run("/bin/rm " + testDir + "/LinkedQueue$Link.class");
            run("/bin/rm " + testDir + "/LinkedQueue$QueueIterator.class");
            run("/bin/rm " + testDir + "/LinkedQueue.class");
            run("/bin/rm " + testDir + "/Queue.class");
            run("/bin/rm " + testDir + "/QueueTest.class");
            
            run(javaHome + "/bin/javac -cp " + testDir + " " + testDir + "/QueueTest.java");
            String output = run(javaHome + "/bin/java -cp " + testDir + " QueueTest");
            boolean pass = !output.contains("Mismatch:") && !output.contains("Could not find or load main class QueueTest") && !output.contains("Exception");
            
            getSender().tell(new Output<A>(pass, (A) input.getAttachment()), getSelf());
          } catch (Exception e) {
            e.printStackTrace();
          }
        })
        .build();
  }
}
