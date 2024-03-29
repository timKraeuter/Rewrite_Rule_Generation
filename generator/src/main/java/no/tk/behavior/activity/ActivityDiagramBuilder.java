package no.tk.behavior.activity;

import java.util.LinkedHashSet;
import java.util.Set;
import no.tk.behavior.activity.edges.ControlFlow;
import no.tk.behavior.activity.nodes.ActivityNode;
import no.tk.behavior.activity.nodes.InitialNode;
import no.tk.behavior.activity.values.Value;
import no.tk.behavior.activity.variables.BooleanVariable;
import no.tk.behavior.activity.variables.Variable;

public class ActivityDiagramBuilder {
  private String name;
  private final Set<Variable<? extends Value>> inputVariables = new LinkedHashSet<>();
  private final Set<Variable<? extends Value>> localVariables = new LinkedHashSet<>();
  private final Set<ActivityNode> nodes = new LinkedHashSet<>();
  private final Set<ControlFlow> edges = new LinkedHashSet<>();
  private InitialNode initialNode;

  public ActivityDiagram build() {
    return new ActivityDiagram(
        this.name,
        this.initialNode,
        this.inputVariables,
        this.localVariables,
        this.nodes,
        this.edges);
  }

  public ActivityDiagramBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public ActivityDiagramBuilder addInputVariable(Variable<? extends Value> inputVar) {
    this.inputVariables.add(inputVar);
    return this;
  }

  public ActivityDiagramBuilder addLocalVariable(Variable<? extends Value> localVar) {
    this.localVariables.add(localVar);
    return this;
  }

  public ActivityDiagramBuilder setInitialNode(InitialNode initialNode) {
    this.initialNode = initialNode;
    return this;
  }

  public ActivityDiagramBuilder addNode(ActivityNode node) {
    this.nodes.add(node);
    return this;
  }

  public ActivityDiagramBuilder createControlFlow(
      String name, ActivityNode source, ActivityNode target) {
    this.createControlFlow(name, source, target, null);
    return this;
  }

  public ActivityDiagramBuilder createControlFlow(
      String name, ActivityNode source, ActivityNode target, BooleanVariable guard) {
    this.addNode(source);
    this.addNode(target);
    ControlFlow controlFlow = new ControlFlow(name, source, target, guard);
    this.edges.add(controlFlow);
    source.addOutgoingControlFlow(controlFlow);
    target.addIncomingControlFlow(controlFlow);
    return this;
  }
}
