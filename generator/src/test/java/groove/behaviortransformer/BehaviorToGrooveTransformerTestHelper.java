package groove.behaviortransformer;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.fixedRules;

import behavior.Behavior;
import groove.graph.GrooveNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import util.FileTestHelper;

public abstract class BehaviorToGrooveTransformerTestHelper {

  public static final String SYSTEM_PROPERTIES_FILE_NAME = "system.properties";
  //  private final String outputPath = FileUtils.getTempDirectoryPath();
  private final String outputPath = "C:/Source/groove/bin";
  boolean REPLACE_EXPECTED_FILES_WITH_ACTUAL = true;

  private Function<String, Boolean> fileNameFilter = x -> false;

  @BeforeEach
  void setUp() {
    GrooveNode.idCounter.set(-1);
    this.fileNameFilter = x -> false;
    this.setUpFurther();
  }

  protected abstract void setUpFurther();

  public abstract String getTestResourcePathSubFolderName();

  public abstract String getOutputPathSubFolderName();

  public String getOutputPathIncludingSubFolder() {
    return this.outputPath + File.separator + this.getOutputPathSubFolderName();
  }

  public void setFileNameFilter(Function<String, Boolean> fileNameFilter) {
    this.fileNameFilter = fileNameFilter;
  }

  public void checkGrooveGeneration(Behavior behavior) throws IOException {
    this.checkGrooveGeneration(behavior, this.fileNameFilter);
  }

  public void checkGrooveGenerationWithIDs(Behavior behavior) throws IOException {
    this.checkGrooveGeneration(behavior, this.fileNameFilter);
  }

  @SuppressWarnings("ConstantConditions")
  private void checkGrooveGeneration(Behavior behavior, Function<String, Boolean> fileNameFilter)
      throws IOException {
    String modelName = behavior.getName();
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
    Path outputDir = Path.of(this.getOutputPathIncludingSubFolder());
    Path grammarDir = transformer.generateGrooveGrammar(behavior, outputDir);
    if (REPLACE_EXPECTED_FILES_WITH_ACTUAL) {
      replaceExpectedFilesWithActual(modelName, grammarDir);
    }

    // assert
    checkGenerationEqualToExpected(fileNameFilter, modelName, grammarDir);

    Path propertiesFile = Path.of(grammarDir.toString(), SYSTEM_PROPERTIES_FILE_NAME);
    this.checkPropertiesFile(propertiesFile);
  }

  private static void replaceExpectedFilesWithActual(String modelName, Path grammarDir)
      throws IOException {
    try (Stream<Path> files = Files.list(grammarDir)) {
      Path expectedDir = Path.of("src/test/resources/bpmn/groove", modelName + ".gps");
      List<Path> generatedFiles =
          files
              .filter(
                  BehaviorToGrooveTransformerTestHelper::ignoreFixedFilesAndSystemPropertiesFile)
              .toList();
      // Copy all remaining files.
      for (Path generatedFile : generatedFiles) {
        Files.copy(
            generatedFile,
            Path.of(expectedDir.toString(), generatedFile.getFileName().toString()),
            StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  private static boolean ignoreFixedFilesAndSystemPropertiesFile(Path path) {
    String fileName = path.getFileName().toString();
    return !(fixedRules.contains(fileName) || fileName.equals(SYSTEM_PROPERTIES_FILE_NAME));
  }

  protected void checkGenerationEqualToExpected(
      Function<String, Boolean> fileNameFilter, String modelName, Path grammarDir)
      throws IOException {
    Path expectedDir = getExpectedFilePathForModel(modelName);
    FileTestHelper.testDirEquals(
        expectedDir,
        grammarDir,
        // Ignore the system.properties file because it contains a timestamp and a dir.
        fileName -> fileName.equals(SYSTEM_PROPERTIES_FILE_NAME) || fileNameFilter.apply(fileName));
  }

  private Path getExpectedFilePathForModel(String modelName) {
    return FileTestHelper.getResource(
        getTestResourcePathSubFolderName() + "/" + modelName + ".gps");
  }

  void checkPropertiesFile(Path propertiesFile) throws IOException {
    Assertions.assertTrue(
        Files.readString(propertiesFile)
            .replaceAll("\r?\n", "\r\n")
            // force identical line separators
            .endsWith("grooveVersion=5.8.1\r\n" + "grammarVersion=3.7"));
  }
}
