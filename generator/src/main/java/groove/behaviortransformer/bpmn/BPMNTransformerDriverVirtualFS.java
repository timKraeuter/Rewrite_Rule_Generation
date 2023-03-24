package groove.behaviortransformer.bpmn;

import static groove.behaviortransformer.bpmn.BPMNTransformerDriver.checkBPMNFilePathIsPresent;
import static groove.behaviortransformer.bpmn.BPMNTransformerDriver.readBPMNFileFromPath;

import behavior.bpmn.BPMNCollaboration;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import groove.behaviortransformer.BehaviorToGrooveTransformer;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

public class BPMNTransformerDriverVirtualFS {

  public static void main(String[] args) throws IOException {
    checkBPMNFilePathIsPresent(args);

    // BPMN file is still read from disk.
    String pathToBPMNFile = args[0];
    BPMNCollaboration bpmnCollaboration = readBPMNFileFromPath(pathToBPMNFile);

    try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
      generateGraphGrammarVirtualFS(bpmnCollaboration, fs.getPath("/virtual"));
    }
  }
  private static Path generateGraphGrammarVirtualFS(
      BPMNCollaboration bpmnCollaboration, Path outputDir) {
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
    Path ggFolder = transformer.generateGrooveGrammar(bpmnCollaboration, outputDir, false);
    System.out.println("Generation finished see " + ggFolder.toString());
    return ggFolder;
  }
}
