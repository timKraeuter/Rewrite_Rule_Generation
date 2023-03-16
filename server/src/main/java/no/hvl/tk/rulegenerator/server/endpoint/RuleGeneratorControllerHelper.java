package no.hvl.tk.rulegenerator.server.endpoint;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviortransformer.BehaviorToGrooveTransformer;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

public class RuleGeneratorControllerHelper {

  public static final String GRAPH_GRAMMAR_TEMP_DIR = getTempDir() + "bpmnAnalyzerGraphGrammars/";
  public static final String STATE_SPACE_TEMP_DIR = getTempDir() + "bpmnAnalyzerStateSpaces/";
  public static final DateTimeFormatter DTF =
      DateTimeFormatter.ofPattern("dd.MM.yyyy-HH.mm.ss").withZone(ZoneId.systemDefault());

  public static final ReentrantLock deleteLock = new ReentrantLock();

  private static String getTempDir() {
    String tempDir = System.getProperty("java.io.tmpdir");
    if (tempDir.endsWith(File.separator)) {
      return tempDir;
    }
    return tempDir + File.separator;
  }

  private RuleGeneratorControllerHelper() {}

  public static void deleteGGsAndStateSpacesOlderThanOneHour() throws IOException {
    deleteLock.lock();
    try {
      // Delete ggs
      deleteTimeStampFilesOlderThanOneHour(GRAPH_GRAMMAR_TEMP_DIR);
      // Delete state spaces
      deleteTimeStampFilesOlderThanOneHour(STATE_SPACE_TEMP_DIR);
    } finally {
      deleteLock.unlock();
    }
  }

  private static void deleteTimeStampFilesOlderThanOneHour(String dirPath) throws IOException {
    File dir = new File(dirPath);
    if (dir.listFiles() == null) {
      return;
    }
    Instant oneHourBefore = Instant.now().minus(1, ChronoUnit.HOURS);
    for (File graphGrammar : dir.listFiles()) {
      deleteIfOlderThan(graphGrammar, oneHourBefore);
    }
  }

  private static void deleteIfOlderThan(File timestampedFile, Instant oneHourBefore)
      throws IOException {
    String timeStampString =
        timestampedFile.getName().substring(0, timestampedFile.getName().indexOf("_"));
    Instant fileTimeStamp = DTF.parse(timeStampString, Instant::from);

    if (fileTimeStamp.isBefore(oneHourBefore)) {
      FileUtils.deleteDirectory(timestampedFile);
    }
  }

  public static Pair<File, BPMNCollaboration> generateGGForBPMNFile(MultipartFile file)
      throws IOException {
    BPMNFileReader bpmnFileReader =
        new BPMNFileReader(BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    BPMNCollaboration bpmnCollaboration = bpmnFileReader.readModelFromStream(file.getInputStream());

    final File grooveGrammarFolder = generateGG(bpmnCollaboration);

    return Pair.of(grooveGrammarFolder, bpmnCollaboration);
  }

  private static File generateGG(BPMNCollaboration bpmnCollaboration) {
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
    String subFolderName =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(bpmnCollaboration.getName());
    File grooveGrammarFolder;
    try {

      grooveGrammarFolder =
          transformer.generateGrooveGrammarForBPMNProcessModel(
              bpmnCollaboration, new File(getGGDirPathname(subFolderName)), false);
    } catch (GrooveGenerationRuntimeException e) {
      // Retry but using ids everywhere.
      grooveGrammarFolder =
          transformer.generateGrooveGrammarForBPMNProcessModel(
              bpmnCollaboration, new File(getGGDirPathname(subFolderName)), true);
    }
    return grooveGrammarFolder;
  }

  private static String getGGDirPathname(String subFolderName) {
    return GRAPH_GRAMMAR_TEMP_DIR + File.separator + subFolderName + File.separator;
  }

  public static String getGGOrStateSpaceDirName(String modelName) {
    return getGGOrStateSpaceDirName(modelName, Instant.now());
  }

  public static String getGGOrStateSpaceDirName(String modelName, Instant time) {
    String timestamp = DTF.format(time.truncatedTo(ChronoUnit.SECONDS));
    return String.format("%s_%s_%s", timestamp, UUID.randomUUID(), modelName);
  }
}
