package behavior.bpmn;

import behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.visitors.DoNothingFlowNodeVisitor;
import behavior.bpmn.events.StartEvent;
import util.ValueWrapper;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EventSubprocess extends AbstractProcess {
    public EventSubprocess(String name,
                           Set<SequenceFlow> sequenceFlows,
                           Set<FlowNode> flowNodes,
                           Set<EventSubprocess> eventSubprocesses) {
        super(name, sequenceFlows, flowNodes, eventSubprocesses);
    }

    @Override
    public void accept(AbstractProcessVisitor visitor) {
        visitor.handle(this);
    }

    public Set<StartEvent> getStartEvents() {
        return this.getFlowNodes().map(flowNode -> {
            ValueWrapper<StartEvent> valueWrapper = new ValueWrapper<>();
            flowNode.accept(new DoNothingFlowNodeVisitor() {
                @Override
                public void handle(StartEvent startEvent) {
                    valueWrapper.setValue(startEvent);
                }
            });
            return valueWrapper.getValueIfExists();
        }).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
