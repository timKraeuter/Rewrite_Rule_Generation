package groove.behaviortransformer.bpmn;

import static org.junit.jupiter.api.Assertions.assertThrows;

import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import util.FileTestHelper;

class BPMNTransformerDriverTest extends BPMNToGrooveTestBase {

  @Test
  void onlyOneArgTest() {
    String[] args = {"123"};
    assertThrows(GrooveGenerationRuntimeException.class, () -> BPMNTransformerDriver.main(args));
  }

  @Test
  void noFileExistsTest() {
    String[] args = {"notAFile", "./"};
    assertThrows(GrooveGenerationRuntimeException.class, () -> BPMNTransformerDriver.main(args));
  }

  @Test
  void mainTest() throws Exception {
    String bpmnFileName = "cyclic.bpmn";
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + bpmnFileName;
    Path pathToBPMNModel = FileTestHelper.getResource(resourcePath);

    String tempDirectoryPath = FileUtils.getTempDirectoryPath();

    String[] args = {pathToBPMNModel.toString(), tempDirectoryPath};

    BPMNTransformerDriver.main(args);

    checkGenerationEqualToExpected(fixedRules::contains, "cyclic", Path.of(tempDirectoryPath));
  }

  @Test
  void mainTestVirtualFS() throws Exception {
    String bpmnFileName = "cyclic.bpmn";
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + bpmnFileName;
    Path pathToBPMNModel = FileTestHelper.getResource(resourcePath);

    String[] args = {pathToBPMNModel.toString(), "a"};

    BPMNTransformerDriverVirtualFS.main(args);
  }
}
