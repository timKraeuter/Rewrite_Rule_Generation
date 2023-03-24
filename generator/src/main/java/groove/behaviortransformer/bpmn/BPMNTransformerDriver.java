package groove.behaviortransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviortransformer.BehaviorToGrooveTransformer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BPMNTransformerDriver {

  public static void main(String[] args) throws IOException {
    checkBPMNFilePathIsPresent(args);

    String pathToBPMNFile = args[0];
    BPMNCollaboration bpmnCollaboration = readBPMNFileFromPath(pathToBPMNFile);

    String outputPath = args[1];
    generateGraphGrammar(bpmnCollaboration, outputPath);
  }

  private static void generateGraphGrammar(BPMNCollaboration bpmnCollaboration, String outputPath) {
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
    Path outputDir = Path.of(outputPath);
    Path file = transformer.generateGrooveGrammar(bpmnCollaboration, outputDir, false);
    System.out.println("Generation finished see " + file.toString());
  }

  static void checkBPMNFilePathIsPresent(String[] args) {
    if (args.length < 2) {
      throw new GrooveGenerationRuntimeException(
          "Please provide to arguments. The first for the input file and the second for the output path.");
    }
  }

  static BPMNCollaboration readBPMNFileFromPath(String pathToBPMNFile) throws IOException {
    Path model = Path.of(pathToBPMNFile);
    if (!Files.exists(model)) {
      throw new GrooveGenerationRuntimeException(
          String.format("No file at the path %s exists.", pathToBPMNFile));
    }
    BPMNFileReader bpmnFileReader =
        new BPMNFileReader(BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    return bpmnFileReader.readModelFromFilePath(model);
  }
}
