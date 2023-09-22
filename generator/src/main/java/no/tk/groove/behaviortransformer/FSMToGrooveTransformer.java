package no.tk.groove.behaviortransformer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.behavior.fsm.FiniteStateMachine;
import no.tk.groove.graph.GrooveGraph;
import no.tk.groove.graph.GrooveGraphBuilder;
import no.tk.groove.graph.GrooveNode;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.groove.graph.rule.GrooveRuleBuilder;
import org.apache.commons.io.file.PathUtils;

public class FSMToGrooveTransformer implements GrooveTransformer<FiniteStateMachine> {

  public static final String FSM_TYPE_GRAPH_DIR = "/StateMachineTypeGraph";

  // Node types
  private static final String TYPE_STATE = TYPE + "State";
  private static final String TYPE_STATE_MACHINE_SNAPSHOT = TYPE + "StateMachineSnapshot";

  // Edge names/attribute names
  private static final String NAME = "name";
  private static final String CURRENT_STATE = "currentState";

  @Override
  public void generateAndWriteRulesFurther(FiniteStateMachine fsm, Path targetFolder) {
    this.copyTypeGraph(targetFolder);
  }

  private void copyTypeGraph(Path targetFolder) {
    try {
      Path sourceDirectory = Paths.get(this.getClass().getResource(FSM_TYPE_GRAPH_DIR).toURI());
      PathUtils.copyDirectory(sourceDirectory, targetFolder, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException | URISyntaxException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
  }

  @Override
  public GrooveGraph generateStartGraph(FiniteStateMachine finiteStateMachine) {
    final String stateMachineName = finiteStateMachine.getName();
    final GrooveGraphBuilder builder = new GrooveGraphBuilder().setName(stateMachineName);
    GrooveNode startStateNode = new GrooveNode(TYPE_STATE);
    GrooveNode stateMachineNode = new GrooveNode(TYPE_STATE_MACHINE_SNAPSHOT);
    builder.addEdge(CURRENT_STATE, stateMachineNode, startStateNode);

    builder.addEdge(
        NAME,
        stateMachineNode,
        new GrooveNode(GrooveTransformerHelper.createStringNodeLabel(stateMachineName)));
    final String startStateName = finiteStateMachine.getStartState().getName();
    builder.addEdge(
        NAME,
        startStateNode,
        new GrooveNode(GrooveTransformerHelper.createStringNodeLabel(startStateName)));

    return builder.build();
  }

  @Override
  public Stream<GrooveGraphRule> generateRules(FiniteStateMachine finiteStateMachine) {
    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    finiteStateMachine
        .getTransitions()
        .forEach(
            transition -> {
              ruleBuilder.startRule(transition.getName());

              final GrooveNode stateMachine = ruleBuilder.contextNode(TYPE_STATE_MACHINE_SNAPSHOT);
              ruleBuilder.contextEdge(
                  NAME,
                  stateMachine,
                  ruleBuilder.contextNode(
                      GrooveTransformerHelper.createStringNodeLabel(finiteStateMachine.getName())));

              final GrooveNode previousState = ruleBuilder.deleteNode(TYPE_STATE);
              ruleBuilder.contextEdge(
                  NAME,
                  previousState,
                  ruleBuilder.contextNode(
                      GrooveTransformerHelper.createStringNodeLabel(
                          transition.getSource().getName())));
              ruleBuilder.deleteEdge(CURRENT_STATE, stateMachine, previousState);

              final GrooveNode newState = ruleBuilder.addNode(TYPE_STATE);
              ruleBuilder.contextEdge(
                  NAME,
                  newState,
                  ruleBuilder.contextNode(
                      GrooveTransformerHelper.createStringNodeLabel(
                          transition.getTarget().getName())));
              ruleBuilder.addEdge(CURRENT_STATE, stateMachine, newState);

              ruleBuilder.buildRule();
            });
    return ruleBuilder.getRules();
  }

  @Override
  public boolean isLayoutActivated() {
    return true; // TODO: implement layout as parameter!
  }
}