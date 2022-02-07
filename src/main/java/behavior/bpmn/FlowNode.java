package behavior.bpmn;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class FlowNode {
    private final String name;
    private final Set<SequenceFlow> outgoingFlows = new LinkedHashSet<>();
    private final Set<SequenceFlow> incomingFlows = new LinkedHashSet<>();

    public FlowNode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addOutgoingSequenceFlow(SequenceFlow outgoingFlow) {
        this.outgoingFlows.add(outgoingFlow);
    }

    public void addIncomingSequenceFlow(SequenceFlow incomingFlow) {
        this.incomingFlows.add(incomingFlow);
    }

    public Stream<SequenceFlow> getOutgoingFlows() {
        return this.outgoingFlows.stream();
    }

    public Stream<SequenceFlow> getIncomingFlows() {
        return this.incomingFlows.stream();
    }

    public abstract void accept(FlowNodeVisitor visitor);

    public abstract boolean isInclusiveGateway();

    public abstract boolean isInstantiateReceiveTask();

    public abstract boolean isTask();

    public abstract boolean isGateway();

    public abstract boolean isExclusiveEventBasedGateway();

    public abstract boolean isMessageOrSignalStartEvent();
}
