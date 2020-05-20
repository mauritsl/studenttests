package nl.ou.edwards;

/**
 * Singleton to store configuration variables.
 */
public final class Config {
  
  private static Config instance;
  private String dataDirectory;
  private String testDirectory;

  /**
   * Constructor.
   */
  private Config() {        
  }
  
  /**
   * Get global instance.
   */
  public static Config getInstance() {
    if (instance == null) {
      instance = new Config();
    }
    return instance;
  }

  /**
   * Set directory where student submissions are stored.
   */
  public void setDataDirectory(String dir) {
    dataDirectory = dir;
  }

  /**
   * Get directory where student submissions are stored.
   */
  public String getDataDirectory() {
    return dataDirectory;
  }

  /**
   * Set directory for writing test files.
   */
  public void setTestDirectory(String dir) {
    testDirectory = dir;
  }

  /**
   * Get directory for writing test files.
   */
  public String getTestDirectory() {
    return testDirectory;
  }
}
