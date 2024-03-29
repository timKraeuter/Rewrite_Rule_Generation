package no.tk.behavior.bpmn;

import no.tk.behavior.bpmn.auxiliary.visitors.FlowElementVisitor;

public class SequenceFlow extends FlowElement {

  public static final String DESCRIPTIVE_NAME_FORMAT = "%s -> %s (%s)";
  public static final String DESCRIPTIVE_NAME_FORMAT_WITHOUT_ID = "%s -> %s";
  private final FlowNode source;
  private final FlowNode target;

  public SequenceFlow(String id, String name, FlowNode source, FlowNode target) {
    super(id, name);
    this.source = source;
    this.target = target;
  }

  @Override
  public void accept(FlowElementVisitor visitor) {
    visitor.handle(this);
  }

  public String getDescriptiveName() {
    return getDescriptiveName(getName(), getId(), source.getName(), target.getName());
  }

  public String getDescriptiveNameWithoutID() {
    if (getName().isEmpty() || getName().isBlank()) {
      return String.format(DESCRIPTIVE_NAME_FORMAT_WITHOUT_ID, source.getName(), target.getName());
    }
    return getName();
  }

  public FlowNode getSource() {
    return source;
  }

  public FlowNode getTarget() {
    return target;
  }

  public static String getDescriptiveName(
      String sfName, String sfID, String sourceName, String targetName) {
    if (sfName.isEmpty() || sfName.isBlank()) {
      return String.format(DESCRIPTIVE_NAME_FORMAT, sourceName, targetName, sfID);
    }
    return String.format("%s (%s)", sfName, sfID);
  }
}
