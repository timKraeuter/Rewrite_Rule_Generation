package no.tk.groove.runner;

import com.google.common.collect.Lists;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import no.tk.groove.runner.checking.ModelCheckingResult;
import no.tk.groove.runner.checking.TemporalLogic;

public class GrooveJarRunner {

  private static final String GROOVE_BIN_DIR = findGrooveBinDir();
  private final boolean printOutputToConsole;

  private static String findGrooveBinDir() {
    List<String> possibleLocations = Lists.newArrayList("./groove/bin", "../groove/bin");
    for (String possibleLocation : possibleLocations) {
      if (Files.exists(Path.of(possibleLocation))) {
        return possibleLocation;
      }
    }
    String currentPath = Path.of("").toAbsolutePath().toString();
    throw new GrooveJarRunnerException(
        String.format("Groove binaries not found in this directory(%s) or above!", currentPath));
  }

  private final String grooveBinDir;

  public GrooveJarRunner() {
    this(true);
  }

  public GrooveJarRunner(boolean printOutputToConsole) {
    this(GROOVE_BIN_DIR, printOutputToConsole);
  }

  private GrooveJarRunner(String grooveBinDir, boolean printOutputToConsole) {
    this.grooveBinDir = grooveBinDir;
    this.printOutputToConsole = printOutputToConsole;
  }

  public Path generateStateSpace(String graphGrammar, String resultFilePath, boolean printOutput)
      throws IOException, InterruptedException {
    // java -jar Generator.jar graphGrammar -o StateSpaceFilePath
    ProcessBuilder builder =
        new ProcessBuilder(
            "java", "-jar", grooveBinDir + "/Generator.jar", graphGrammar, "-o", resultFilePath);

    runProcess(printOutput, builder);
    return Path.of(resultFilePath);
  }

  public ModelCheckingResult checkCTL(String graphGrammar, String ctlProperty)
      throws IOException, InterruptedException {
    // java -jar ModelChecker.jar graphGrammar -ctl ctlProperty
    ProcessBuilder builder =
        new ProcessBuilder(
            "java", "-jar", grooveBinDir + "/ModelChecker.jar", graphGrammar, "-ctl", ctlProperty);
    String grooveOutput = runProcessAndReturnOutput(builder);
    return readModelCheckingResultFromGrooveOutput(grooveOutput, ctlProperty);
  }

  private ModelCheckingResult readModelCheckingResultFromGrooveOutput(
      String output, String ctlProperty) {
    boolean valid = output.contains(": satisfied");
    String error = "";
    if (containsError(output)) {
      error = getError(output);
    }
    return new ModelCheckingResult(TemporalLogic.CTL, ctlProperty, valid, error);
  }

  private String getError(String output) {
    return output
        .substring(output.indexOf("Error: "), output.indexOf("Usage: "))
        .replace("\r\n", "")
        .replace("\n", "");
  }

  private static boolean containsError(String output) {
    return output.contains("Error: ");
  }

  private void runProcess(boolean printOutput, ProcessBuilder builder)
      throws IOException, InterruptedException {
    builder.redirectErrorStream(true);
    Process process = builder.start();
    if (printOutput) {
      new Thread(() -> printOutput(process)).start();
    }
    process.waitFor(60, TimeUnit.SECONDS);
    process.destroy(); // no op if already stopped.
    process.waitFor();
  }

  private String runProcessAndReturnOutput(ProcessBuilder builder)
      throws IOException, InterruptedException {
    builder.redirectErrorStream(true);

    Process process = builder.start();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    new Thread(() -> printOutput(process, output)).start();

    process.waitFor(60, TimeUnit.SECONDS);
    process.destroy(); // no op if already stopped.
    process.waitFor();

    return output.toString(StandardCharsets.UTF_8);
  }

  private void printOutput(Process p) {
    printOutput(p, null);
  }

  private void printOutput(Process p, ByteArrayOutputStream output) {
    boolean saveOutput = output != null;
    try {
      byte[] buffer = new byte[1024];
      for (int length; (length = p.getInputStream().read(buffer)) != -1; ) {
        if (this.printOutputToConsole) {
          System.out.write(buffer, 0, length);
        }
        if (saveOutput) {
          output.write(buffer, 0, length);
        }
      }
    } catch (IOException e) {
      System.out.println("Process output could not be read! " + e.getMessage());
    }
  }
}
