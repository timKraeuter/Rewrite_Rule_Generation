package behavior.bpmn;

import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a process modeled in BPMN.
 */
public class Process {
    private final String name;
    private final StartEvent startEvent;
    private final Set<SequenceFlow> sequenceFlows;

    public Process(String name, StartEvent startEvent, Set<SequenceFlow> sequenceFlows) {
        this.name = name;
        this.startEvent = startEvent;
        this.sequenceFlows = sequenceFlows;
    }

    public StartEvent getStartEvent() {
        return this.startEvent;
    }

    public Stream<FlowNode> getControlFlowNodes() {
        final LinkedHashSet<FlowNode> nodes = new LinkedHashSet<>();
        sequenceFlows.forEach(sequenceFlow -> {
            nodes.add(sequenceFlow.getSource());
            nodes.add(sequenceFlow.getTarget());
        });
        return nodes.stream();
    }

    public Stream<Process> getSubProcesses() {
        final LinkedHashSet<Process> subProcesses = new LinkedHashSet<>();
        getControlFlowNodes().forEach(flowNode -> flowNode.accept(new FlowNodeVisitor() {
            @Override
            public void handle(Task task) {
            }

            @Override
            public void handle(SendTask task) {
            }

            @Override
            public void handle(ReceiveTask task) {
            }

            @Override
            public void handle(CallActivity callActivity) {
                subProcesses.add(callActivity.getSubProcessModel());
                subProcesses.addAll(callActivity.getSubProcessModel().getSubProcesses().collect(Collectors.toList()));
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
            }

            @Override
            public void handle(ParallelGateway parallelGateway) {
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
            }

            @Override
            public void handle(StartEvent startEvent) {
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
            }

            @Override
            public void handle(EndEvent endEvent) {
            }

            @Override
            public void handle(EventBasedGateway eventBasedGateway) {
            }
        }));
        return subProcesses.stream();
    }

    public String getName() {
        return name;
    }

    /*
    According to the BPMN spec the following consistency rules exist:
    - Gateways or Activities without incoming sequence flows are forbidden (p426)
     */
}
