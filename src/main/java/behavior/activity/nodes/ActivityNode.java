package behavior.activity.nodes;

import behavior.activity.edges.ControlFlow;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ActivityNode {
    private final String name;
    private Set<ControlFlow> outgoingFlows = new LinkedHashSet<>();
    private Set<ControlFlow> incomingFlows = new LinkedHashSet<>();

    protected ActivityNode(String name) {
        this.name = name;
    }

    public void addOutgoingControlFlow(ControlFlow outgoingFlow) {
        this.outgoingFlows.add(outgoingFlow);
    }

    public void addIncomingControlFlow(ControlFlow incomingFlow) {
        this.incomingFlows.add(incomingFlow);
    }

    public String getName() {
        return this.name;
    }

    public Stream<ControlFlow> getOutgoingFlows() {
        return this.outgoingFlows.stream();
    }

    public Stream<ControlFlow> getIncomingFlows() {
        return this.incomingFlows.stream();
    }

    public abstract void accept(ActivityNodeVisitor visitor);

    public abstract boolean isDecisionNode();
}