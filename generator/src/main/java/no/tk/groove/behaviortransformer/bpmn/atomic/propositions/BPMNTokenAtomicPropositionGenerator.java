package no.tk.groove.behaviortransformer.bpmn.atomic.propositions;

import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.contextProcessInstanceWithOnlyName;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.contextTokenWithPosition;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.nacTokenWithPosition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import no.tk.behavior.bpmn.reader.token.model.CollaborationSnapshot;
import no.tk.behavior.bpmn.reader.token.model.ProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.Token;
import no.tk.groove.graph.GrooveNode;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.groove.graph.rule.GrooveRuleBuilder;
import no.tk.groove.graph.rule.GrooveRuleWriter;

public class BPMNTokenAtomicPropositionGenerator {

  private final boolean layout;

  public BPMNTokenAtomicPropositionGenerator(boolean layout) {
    this.layout = layout;
  }

  public void generateAndWriteAtomicProposition(
      CollaborationSnapshot collaborationSnapshot, Path outputFolder) throws IOException {
    GrooveGraphRule graphRule = generateAtomicProposition(collaborationSnapshot);
    Files.createDirectories(outputFolder);
    GrooveRuleWriter.writeRules(outputFolder, Stream.of(graphRule), layout);
  }

  public GrooveGraphRule generateAtomicProposition(CollaborationSnapshot collaborationSnapshot) {
    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    ruleBuilder.startRule(collaborationSnapshot.getName());
    collaborationSnapshot
        .getProcessSnapshots()
        .forEach(processSnapshot -> generateNodesForSnapshot(processSnapshot, ruleBuilder));

    return ruleBuilder.buildRule();
  }

  private void generateNodesForSnapshot(
      ProcessSnapshot processSnapshot, GrooveRuleBuilder ruleBuilder) {
    GrooveNode snapshotNode = generateNodeForSnapshot(processSnapshot, ruleBuilder);
    // We need the previous node for connecting them.
    processSnapshot
        .getTokens()
        .forEach(token -> generateNodeForToken(token, snapshotNode, ruleBuilder));
  }

  private void generateNodeForToken(
      Token token, GrooveNode snapshotNode, GrooveRuleBuilder ruleBuilder) {
    if (token.isShouldExist()) {
      contextTokenWithPosition(ruleBuilder, snapshotNode, token.getElementID());
    } else {
      nacTokenWithPosition(ruleBuilder, snapshotNode, token.getElementID());
    }
  }

  private GrooveNode generateNodeForSnapshot(
      ProcessSnapshot processSnapshot, GrooveRuleBuilder ruleBuilder) {
    return contextProcessInstanceWithOnlyName(processSnapshot.getProcessName(), ruleBuilder);
  }
}
