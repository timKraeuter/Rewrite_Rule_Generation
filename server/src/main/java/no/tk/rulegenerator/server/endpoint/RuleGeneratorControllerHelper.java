package no.tk.rulegenerator.server.endpoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.reader.BPMNFileReader;
import no.tk.behavior.bpmn.reader.token.BPMNTokenFileReader;
import no.tk.behavior.bpmn.reader.token.model.CollaborationSnapshot;
import no.tk.groove.behaviortransformer.BehaviorToGrooveTransformer;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import no.tk.groove.behaviortransformer.bpmn.atomic.propositions.BPMNTokenAtomicPropositionGenerator;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNProposition;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class RuleGeneratorControllerHelper {
  protected static final Logger logger =
      LoggerFactory.getLogger(RuleGeneratorControllerHelper.class);

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
    Path dir = Path.of(dirPath);
    if (!Files.exists(dir)) {
      return;
    }
    Instant oneHourBefore = Instant.now().minus(1, ChronoUnit.HOURS);
    try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
      for (Path graphGrammar : files) {
        deleteIfOlderThan(graphGrammar, oneHourBefore);
      }
    }
  }

  private static void deleteIfOlderThan(Path timestampedFile, Instant oneHourBefore)
      throws IOException {
    String timeStampString =
        timestampedFile
            .getFileName()
            .toString()
            .substring(0, timestampedFile.getFileName().toString().indexOf("_"));
    Instant fileTimeStamp = DTF.parse(timeStampString, Instant::from);

    if (fileTimeStamp.isBefore(oneHourBefore)) {
      logger.info("Deleting old generated files at {}", timestampedFile);
      PathUtils.delete(timestampedFile);
    }
  }

  public static Pair<Path, BPMNCollaboration> generateGGForBPMNFile(
      MultipartFile file, boolean layout) throws IOException {
    BPMNFileReader bpmnFileReader =
        new BPMNFileReader(BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    BPMNCollaboration bpmnCollaboration = bpmnFileReader.readModelFromStream(file.getInputStream());

    final Path grooveGrammarFolder = generateGG(bpmnCollaboration, layout);

    return Pair.of(grooveGrammarFolder, bpmnCollaboration);
  }

  private static Path generateGG(BPMNCollaboration bpmnCollaboration, boolean layout) {
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer(layout);
    String subFolderName =
        RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(bpmnCollaboration.getName());
    return transformer.generateGrooveGrammarForBPMNProcessModel(
        bpmnCollaboration, Path.of(getGGDirPathname(subFolderName)));
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

  public static void generatePropositions(Path ggFolder, Set<BPMNProposition> props, boolean layout)
      throws IOException {
    BPMNTokenAtomicPropositionGenerator generator = new BPMNTokenAtomicPropositionGenerator(layout);
    BPMNTokenFileReader bpmnTokenFileReader =
        new BPMNTokenFileReader(
            BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    for (BPMNProposition prop : props) {
      CollaborationSnapshot collaborationSnapshot =
          bpmnTokenFileReader.readModelFromString(prop.name(), prop.xml());

      generator.generateAndWriteAtomicProposition(collaborationSnapshot, ggFolder);
    }
  }
}
