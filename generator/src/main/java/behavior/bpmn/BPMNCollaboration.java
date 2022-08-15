package behavior.bpmn;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import behavior.bpmn.events.BoundaryEvent;
import behavior.bpmn.events.Event;
import behavior.bpmn.events.EventDefinition;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BPMNCollaboration implements Behavior {
    private final String name;
    private final Set<Process> participants;
    /**
     * Derived from call activities of the participants.
     */
    private final Set<Process> subprocesses;
    private final Set<MessageFlow> messageFlows;

    public BPMNCollaboration(String name,
                             Set<Process> participants,
                             Set<Process> subprocesses,
                             Set<MessageFlow> messageFlows) {
        this.name = name;
        this.participants = participants;
        this.subprocesses = subprocesses;
        this.messageFlows = messageFlows;
    }

    public Set<Process> getParticipants() {
        return participants;
    }

    public Set<MessageFlow> getMessageFlows() {
        return messageFlows;
    }


    public Set<MessageFlow> outgoingMessageFlows(FlowNode producingMessageFlowNode) {
        return this.getMessageFlows().stream()
                   .filter(messageFlow -> messageFlow.getSource() == producingMessageFlowNode)
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<MessageFlow> getIncomingMessageFlows(FlowNode messageTarget) {
        return this.getMessageFlows().stream()
                   .filter(flow -> flow.getTarget().equals(messageTarget))
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Process getMessageFlowReceiverProcess(MessageFlow flow) {
        // TODO: refactor use/merge with findProcessForFlowNode below.
        // subprocesses recursively contains all subprocesses but what about event subprocesses?
        // Event subprocesses seems to be currently missing here.
        Optional<Process> optionalProcess = this.getParticipants().stream()
                                                .filter(process -> process.getFlowNodes().anyMatch(flowNode ->
                                                                                                           flowNode ==
                                                                                                           flow.getTarget()))
                                                .findFirst();
        if (optionalProcess.isPresent()) {
            return optionalProcess.get();
        }
        // The message flow must go to a subprocess!.
        return subprocesses.stream()
                           .filter(process -> process.getFlowNodes().anyMatch(flowNode -> flowNode ==
                                                                                          flow.getTarget()))
                           .findFirst().orElseThrow(() -> new RuntimeException("Message flow receiver not found!"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void accept(BehaviorVisitor visitor) {
        visitor.handle(this);
    }

    public AbstractProcess getParentProcessForEventSubprocess(EventSubprocess eventSubprocess) {
        // TODO: Digg multiple levels deep!
        final Optional<Process> foundParentProcess = this.participants.stream()
                                                                      .filter(process -> process.getEventSubprocesses()
                                                                                                .anyMatch(
                                                                                                        eventSubprocess1 -> eventSubprocess1.equals(
                                                                                                                eventSubprocess)))
                                                                      .findFirst();
        if (foundParentProcess.isPresent()) {
            return foundParentProcess.get();
        }
        throw new ShouldNotHappenRuntimeException("Parent process could not be found for event subprocess!" +
                                                  eventSubprocess);
    }


    public AbstractProcess findProcessForFlowNode(FlowNode flowNode) {
        for (Process participant : this.getParticipants()) {
            final boolean processFound =
                    participant.getFlowNodes().anyMatch(subProcessFlowNode -> subProcessFlowNode.equals(flowNode));
            if (processFound) {
                return participant;
            }
            // TODO: Similar to getMessageFlowReceiverProcess. Refactor!
            // TODO: Subprocesses of subprocesses?
            Optional<Process> optionalSubprocess =
                    participant.getSubProcesses().filter(subprocess -> subprocess.getFlowNodes().anyMatch(
                            subProcessFlowNode -> subProcessFlowNode.equals(flowNode))).findFirst();
            if (optionalSubprocess.isPresent()) {
                return optionalSubprocess.get();
            }
            final Optional<EventSubprocess> optionalEventSubprocess = participant.getEventSubprocesses().filter(
                    eventSubprocess -> eventSubprocess.getStartEvents().stream().anyMatch(startEvent -> startEvent.equals(
                            flowNode))).findFirst();
            if (optionalEventSubprocess.isPresent()) {
                return optionalEventSubprocess.get();
            }
        }
        // Should not happen.
        throw new ShouldNotHappenRuntimeException(String.format("No process for the flow node %s found!", flowNode));
    }


    public Pair<Set<Event>, Set<BoundaryEvent>> findAllCorrespondingSignalCatchEvents(EventDefinition eventDefinition) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<BoundaryEvent> signalBoundaryCatchEvents = new LinkedHashSet<>();
        Set<Process> seenProcesses = new HashSet<>();
        this.getParticipants().forEach(process -> {
            Pair<Set<Event>, Set<BoundaryEvent>> signalAndSignalBoundaryCatchEvents =
                    findAllCorrespondingSignalCatchEvents(
                            process,
                            eventDefinition,
                            seenProcesses);
            signalCatchEvents.addAll(signalAndSignalBoundaryCatchEvents.getLeft());
            signalBoundaryCatchEvents.addAll(signalAndSignalBoundaryCatchEvents.getRight());
        });
        return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
    }

    Pair<Set<Event>, Set<BoundaryEvent>> findAllCorrespondingSignalCatchEvents(Process process,
                                                                               EventDefinition eventDefinition,
                                                                               Set<Process> seenProcesses) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<BoundaryEvent> signalBoundaryCatchEvents = new LinkedHashSet<>();
        if (seenProcesses.contains(process)) {
            return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
        }
        seenProcesses.add(process);

        process.getFlowNodes().forEach(flowNode -> flowNode.accept(new SignalCatchEventFlowNodeVisitor(this,
                                                                                                       eventDefinition,
                                                                                                       signalCatchEvents,
                                                                                                       signalBoundaryCatchEvents,
                                                                                                       seenProcesses)

        ));
        process.getEventSubprocesses().forEach(eventSubprocess -> eventSubprocess.getFlowNodes().forEach(flowNode -> flowNode.accept(
                new SignalCatchEventFlowNodeVisitor(this,
                                                    eventDefinition,
                                                    signalCatchEvents,
                                                    signalBoundaryCatchEvents,
                                                    seenProcesses))));
        return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
    }
}
