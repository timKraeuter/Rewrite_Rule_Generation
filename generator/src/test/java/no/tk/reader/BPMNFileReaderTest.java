package no.tk.reader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.tk.behavior.bpmn.*;
import no.tk.behavior.bpmn.activities.Activity;
import no.tk.behavior.bpmn.activities.CallActivity;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.Task;
import no.tk.behavior.bpmn.events.*;
import no.tk.behavior.bpmn.events.definitions.EventDefinition;
import no.tk.behavior.bpmn.events.definitions.LinkEventDefinition;
import no.tk.behavior.bpmn.events.definitions.SignalEventDefinition;
import no.tk.behavior.bpmn.gateways.EventBasedGateway;
import no.tk.behavior.bpmn.reader.BPMNFileReader;
import no.tk.util.FileTestHelper;
import org.junit.jupiter.api.Test;

class BPMNFileReaderTest implements BPMNFileReaderTestHelper {

  public static final String READER_TEST_PATH = "bpmn/bpmnModelsReaderTest/";

  @Test
  void readTasks() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "tasks.bpmn");

    // Expect the model shown here: https://cawemo.com/share/882d7c5b-bff0-4244-a39f-a234795035e5
    assertNotNull(result);
    String name = "tasks";
    assertThat(result.getName(), is(name));
    // No pools so only one participant.
    assertThat(result.getParticipants().size(), is(1));
    BPMNProcess participant = result.getParticipants().iterator().next();
    assertThat(participant.getName(), is(name));

    assertThat(participant.sequenceFlows().count(), is(10L));
    assertThat(participant.flowNodes().count(), is(11L));
    // Sequence flows between the right flow nodes.
    Set<String> sfDescriptiveNames = getSequenceFlowDescriptiveNamesForProcess(participant);
    assertThat(
        sfDescriptiveNames,
        is(
            Sets.newHashSet(
                "start -> task (Flow_08k9gv1)",
                "task -> sendTask (Flow_1a1i07n)",
                "sendTask -> receiveTask (Flow_0zbc6ny)",
                "receiveTask -> userTask (Flow_1p412ok)",
                "userTask -> manualTask (Flow_0u1hqoj)",
                "manualTask -> businessRTask (Flow_1my2hks)",
                "businessRTask -> serviceTask (Flow_1favftk)",
                "serviceTask -> scriptTask (Flow_1d05hhp)",
                "scriptTask -> subprocess (Flow_16xvne2)",
                "subprocess -> end (Flow_1kb5ejz)")));

    // Check instantiate receive task
    Map<String, FlowNode> flowNodes = createFlowNodeNameToFlowNodeMap(participant);
    String receiveTaskName = "receiveTask";
    FlowNode instantiateReceiveTask = flowNodes.get(receiveTaskName);
    // Instantiate must be true!
    assertThat(
        instantiateReceiveTask, is(new ReceiveTask("Activity_0xr3zq4", receiveTaskName, true)));
  }

  @Test
  void readUnconnectedElements() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "unconnected.bpmn");

    // Expect the model shown here: https://cawemo.com/share/11f6314a-43f9-475d-94ae-149ad85119c1
    assertNotNull(result);
    assertThat(result.getParticipants().size(), is(1));
    BPMNProcess participant = result.getParticipants().iterator().next();
    assertThat(participant.sequenceFlows().count(), is(0L));
    assertThat(participant.flowNodes().count(), is(3L));
    // Check flow nodes
    Map<String, FlowNode> flowNodes = createFlowNodeNameToFlowNodeMap(participant);
    assertThat(flowNodes.keySet(), is(Sets.newHashSet("start", "Task", "end")));
  }

  @Test
  void readGateways() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "gateways.bpmn");

    // Expect the model shown here: https://cawemo.com/share/bfb5f9e4-1b24-4ff7-bee1-278ea6aa80bc
    assertNotNull(result);
    String name = "gateways";
    assertThat(result.getName(), is(name));
    // No pools so only one participant.
    assertThat(result.getParticipants().size(), is(1));
    BPMNProcess participant = result.getParticipants().iterator().next();
    assertThat(participant.getName(), is(name));

    assertThat(participant.sequenceFlows().count(), is(4L));
    assertThat(participant.flowNodes().count(), is(5L));

    // Sequence flows between the right flow nodes.
    Set<String> sFDescriptiveNames = getSequenceFlowDescriptiveNamesForProcess(participant);
    assertThat(
        sFDescriptiveNames,
        is(
            Sets.newHashSet(
                "inclusive gateway -> parallel gateway (Flow_1u29twe)",
                "parallel gateway -> exclusive gateway (Flow_0a74e3j)",
                "exclusive gateway -> event gateway (Flow_16wjsu0)",
                "exclusive gateway -> instantiate event gateway (Flow_0kw6o9f)")));

    // Check if instantiate was read correctly for the event gateway.
    Map<String, FlowNode> flowNodes = createFlowNodeNameToFlowNodeMap(participant);
    String evGatewayName = "instantiate event gateway";
    FlowNode instantiate_event_gateway = flowNodes.get(evGatewayName);
    assertThat(
        instantiate_event_gateway,
        is(new EventBasedGateway(instantiate_event_gateway.getId(), evGatewayName, true)));
  }

  @Test
  void readEvents() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "events.bpmn");

    // Expect the model shown here: https://cawemo.com/share/19b961cd-d4e2-4af8-8994-2e43e7ed094b
    assertNotNull(result);
    String name = "events";
    assertThat(result.getName(), is(name));
    // No pools so only one participant.
    assertThat(result.getParticipants().size(), is(1));
    BPMNProcess participant = result.getParticipants().iterator().next();
    assertThat(participant.getName(), is(name));

    assertThat(participant.sequenceFlows().count(), is(18L));
    assertThat(participant.flowNodes().count(), is(21L));
    // Sequence flows between the right flow nodes.
    Set<String> sequenceFlowIds = getSequenceFlowDescriptiveNamesForProcess(participant);
    assertThat(
        sequenceFlowIds,
        is(
            Sets.newHashSet(
                "start -> e1 (Flow_0xvlhi9)",
                "messageStart -> e1 (Flow_06phty5)",
                "signalStart -> e1 (Flow_0pbgilo)",
                "e3 -> messageEnd (Flow_15dm2o2)",
                "e3 -> signalEnd (Flow_07caac4)",
                "e3 -> terminateEnd (Flow_0lr34nm)",
                "e3 -> end (Flow_0pujm76)",
                "e3 -> errorEnd (Flow_1tsjahm)",
                "e3 -> escalationEnd (Flow_1jay14p)",
                "linkCEvent -> e2 (Flow_0e537m9)",
                "timerCEvent -> e2 (Flow_1a8axf3)",
                "intermediateEvent -> e2 (Flow_0tkjq4a)",
                "messageCEvent -> e2 (Flow_0ec0gml)",
                "messageTEvent -> e2 (Flow_1vt0ayl)",
                "e2 -> linkTEvent (Flow_0u4h2kh)",
                "signalCEvent -> e2 (Flow_03b2k4q)",
                "signalTEvent -> e2 (Flow_1gn5e8b)",
                "escalationTEvent -> e2 (Flow_0ba6hnc)")));

    Map<String, FlowNode> flowNodes = createFlowNodeNameToFlowNodeMap(participant);
    // Check start events
    String startEventName = "start";
    FlowNode startEvent = flowNodes.get(startEventName);
    assertThat(startEvent, is(new StartEvent(startEvent.getId(), startEventName)));
    String messageStartEventName = "messageStart";
    FlowNode messageStartEvent = flowNodes.get(messageStartEventName);
    assertThat(
        messageStartEvent,
        is(
            new StartEvent(
                messageStartEvent.getId(), messageStartEventName, StartEventType.MESSAGE)));
    String signalStartEventName = "signalStart";
    String startEndSignalEventDefinition = "Signal_113rm6n";
    FlowNode signalStartEvent = flowNodes.get(signalStartEventName);
    assertThat(
        signalStartEvent,
        is(
            new StartEvent(
                signalStartEvent.getId(),
                signalStartEventName,
                StartEventType.SIGNAL,
                new SignalEventDefinition(startEndSignalEventDefinition),
                true)));

    // Check intermediate events
    String intermediateEventName = "intermediateEvent";
    FlowNode intermediateEvent = flowNodes.get(intermediateEventName);
    assertThat(
        intermediateEvent,
        is(
            new IntermediateThrowEvent(
                intermediateEvent.getId(),
                intermediateEventName,
                IntermediateThrowEventType.NONE)));
    String messageCEventName = "messageCEvent";
    FlowNode messageCatchEvent = flowNodes.get(messageCEventName);
    assertThat(
        messageCatchEvent,
        is(
            new IntermediateCatchEvent(
                messageCatchEvent.getId(), messageCEventName, IntermediateCatchEventType.MESSAGE)));
    String messageTEventName = "messageTEvent";
    FlowNode messageTEvent = flowNodes.get(messageTEventName);
    assertThat(
        messageTEvent,
        is(
            new IntermediateThrowEvent(
                messageTEvent.getId(), messageTEventName, IntermediateThrowEventType.MESSAGE)));
    String linkCEventName = "linkCEvent";
    FlowNode linkCEvent = flowNodes.get(linkCEventName);
    assertThat(
        linkCEvent,
        is(
            new IntermediateCatchEvent(
                linkCEvent.getId(),
                linkCEventName,
                IntermediateCatchEventType.LINK,
                new LinkEventDefinition("123"))));
    String signalCEventName = "signalCEvent";
    String intermediateSignalEventDefinition = "Signal_1ni52ju";
    FlowNode signalCEvent = flowNodes.get(signalCEventName);
    assertThat(
        signalCEvent,
        is(
            new IntermediateCatchEvent(
                signalCEvent.getId(),
                signalCEventName,
                IntermediateCatchEventType.SIGNAL,
                new SignalEventDefinition(intermediateSignalEventDefinition))));
    String signalTEventName = "signalTEvent";
    FlowNode signalTEvent = flowNodes.get(signalTEventName);
    assertThat(
        signalTEvent,
        is(
            new IntermediateThrowEvent(
                signalTEvent.getId(),
                signalTEventName,
                IntermediateThrowEventType.SIGNAL,
                new SignalEventDefinition(intermediateSignalEventDefinition))));
    String linkTEventName = "linkTEvent";
    FlowNode linkTEvent = flowNodes.get(linkTEventName);
    assertThat(
        linkTEvent,
        is(
            new IntermediateThrowEvent(
                linkTEvent.getId(),
                linkTEventName,
                IntermediateThrowEventType.LINK,
                new LinkEventDefinition("123"))));
    String timerCEventName = "timerCEvent";
    FlowNode timerCEvent = flowNodes.get(timerCEventName);
    assertThat(
        timerCEvent,
        is(
            new IntermediateCatchEvent(
                timerCEvent.getId(), timerCEventName, IntermediateCatchEventType.TIMER)));
    // Check end events
    String endEventName = "end";
    FlowNode endEvent = flowNodes.get(endEventName);
    assertThat(endEvent, is(new EndEvent(endEvent.getId(), endEventName)));
    String messageEndEventName = "messageEnd";
    FlowNode messageEndEvent = flowNodes.get(messageEndEventName);
    assertThat(
        messageEndEvent,
        is(new EndEvent(messageEndEvent.getId(), messageEndEventName, EndEventType.MESSAGE)));
    String signalEndEventName = "signalEnd";
    FlowNode signalEndEvent = flowNodes.get(signalEndEventName);
    assertThat(
        signalEndEvent,
        is(
            new EndEvent(
                signalEndEvent.getId(),
                signalEndEventName,
                EndEventType.SIGNAL,
                new SignalEventDefinition(startEndSignalEventDefinition))));
    String terminateEndEventName = "terminateEnd";
    FlowNode terminateEndEvent = flowNodes.get(terminateEndEventName);
    assertThat(
        terminateEndEvent,
        is(
            new EndEvent(
                terminateEndEvent.getId(), terminateEndEventName, EndEventType.TERMINATION)));
    String errorEndEventName = "errorEnd";
    FlowNode errorEndEvent = flowNodes.get(errorEndEventName);
    assertThat(
        errorEndEvent,
        is(new EndEvent(errorEndEvent.getId(), errorEndEventName, EndEventType.ERROR)));
    String escalationEndEventName = "escalationEnd";
    FlowNode escalationEndEvent = flowNodes.get(escalationEndEventName);
    assertThat(
        escalationEndEvent,
        is(
            new EndEvent(
                escalationEndEvent.getId(), escalationEndEventName, EndEventType.ESCALATION)));
  }

  @Test
  void readBoundaryEvents() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "boundary-events.bpmn");

    // Expect the model shown here: https://cawemo.com/share/9831098e-dba7-446c-9f35-d1af37295551
    assertNotNull(result);
    String name = "boundary-events";
    assertThat(result.getName(), is(name));
    // No pools so only one participant.
    assertThat(result.getParticipants().size(), is(2));
    Iterator<BPMNProcess> it = result.getParticipants().iterator();
    BPMNProcess participant1 = it.next();
    BPMNProcess participant2 = it.next();
    assertThat(participant2.getName(), is("Activity boundary events"));
    assertThat(participant2.sequenceFlows().count(), is(12L));
    assertThat(participant2.flowNodes().count(), is(23L));

    // Check boundary events for activity 1 and 2.
    Map<String, FlowNode> p2flowNodes = createFlowNodeNameToFlowNodeMap(participant2);
    String activity1Name = "Activity1";
    FlowNode activity1 = p2flowNodes.get(activity1Name);
    Task activity1Expected = new Task(activity1.getId(), activity1Name);
    activity1Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_0f3lbm7", "n1", BoundaryEventType.NONE, true));
    activity1Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_0lmpr4f", "s1", BoundaryEventType.SIGNAL, true));
    activity1Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_0bz0xda", "m1", BoundaryEventType.MESSAGE, true));
    activity1Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_0h1b9o9", "t1", BoundaryEventType.TIMER, true));
    activity1Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_07foo28", "er1", BoundaryEventType.ERROR, true));
    activity1Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_1rxhdtp", "es1", BoundaryEventType.ESCALATION, true));

    assertThat(activity1Expected, is(activity1));
    assertThat(
        ((Activity) activity1).getBoundaryEvents(), is(activity1Expected.getBoundaryEvents()));

    String activity2Name = "Activity2";
    FlowNode activity2 = p2flowNodes.get(activity2Name);
    Task activity2Expected = new Task(activity2.getId(), activity2Name);
    activity2Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_1knnzz6", "m2", BoundaryEventType.MESSAGE, false));
    activity2Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_1bl83g8", "t2", BoundaryEventType.TIMER, false));
    activity2Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_1mu3twa", "s2", BoundaryEventType.SIGNAL, false));
    activity2Expected.attachBoundaryEvent(
        new BoundaryEvent("Event_0l60im2", "es2", BoundaryEventType.ESCALATION, false));

    assertThat(activity2, is(activity2Expected));
    assertThat(
        ((Activity) activity2).getBoundaryEvents(), is(activity2Expected.getBoundaryEvents()));

    Set<String> sFDescriptiveNames = getSequenceFlowDescriptiveNamesForProcess(participant2);
    assertThat(
        sFDescriptiveNames,
        is(
            Sets.newHashSet(
                "start -> Activity1 (Flow_1f4z70z)",
                "s1 -> s1_end (Flow_1udtcnz)",
                "n1 -> n1_end (Flow_1h5llv5)",
                "t1 -> t1_end (Flow_1jzbhgq)",
                "m1 -> m1_end (Flow_0y1lybr)",
                "er1 -> er1_end (Flow_062pd7r)",
                "es1 -> es1_end (Flow_1iwa9a3)",
                "Activity1 -> Activity2 (Flow_1aehxox)",
                "m2 -> m2_end (Flow_08bu3t7)",
                "t2 -> t2_end (Flow_11gyf74)",
                "s2 -> s2_end (Flow_11orhgw)",
                "es2 -> es2_end (Flow_0ce9j37)")));

    assertThat(participant1.getName(), is("Subprocess boundary events"));
    assertThat(participant1.sequenceFlows().count(), is(13L));
    assertThat(participant1.flowNodes().count(), is(24L));

    // Check boundary events for subprocess s1 and s2.
    Map<String, FlowNode> p1FlowNodes = createFlowNodeNameToFlowNodeMap(participant1);
    CallActivity subprocess1 = (CallActivity) p1FlowNodes.get("S1");
    CallActivity subprocess2 = (CallActivity) p1FlowNodes.get("S2");
    // S1 has the same boundary events as activity 1 same for s2 and activity 2.
    assertThat(subprocess1.getBoundaryEvents().size(), is(6));
    assertTrue(
        subprocess1
            .getBoundaryEvents()
            .contains(new BoundaryEvent("Event_1wiourw", "n1", BoundaryEventType.NONE, true)));
    assertThat(subprocess2.getBoundaryEvents().size(), is(4));
    assertTrue(
        subprocess2
            .getBoundaryEvents()
            .contains(new BoundaryEvent("Event_1cwazog", "m2", BoundaryEventType.MESSAGE, false)));
  }

  private Set<String> getSequenceFlowDescriptiveNamesForProcess(BPMNProcess participant) {
    return participant
        .sequenceFlows()
        .map(SequenceFlow::getDescriptiveName)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Test
  void readPoolsAndMessageFlows() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "pools-message-flows.bpmn");

    // Expect the model shown here: https://cawemo.com/share/a7b1034d-da01-4afd-afdc-26cfdb33ef06
    assertNotNull(result);
    assertThat(result.getName(), is("pools-message-flows"));
    // Two pools = two participants.
    assertThat(result.getParticipants().size(), is(2));
    Iterator<BPMNProcess> it = result.getParticipants().iterator();

    // Check p1
    BPMNProcess participant1 = it.next();
    assertThat(participant1.getName(), is("p1"));
    assertThat(participant1.sequenceFlows().count(), is(3L));
    assertThat(participant1.flowNodes().count(), is(4L));

    // Check p2
    BPMNProcess participant2 = it.next();
    assertThat(participant2.getName(), is("p2"));
    assertThat(participant2.sequenceFlows().count(), is(3L));
    assertThat(participant2.flowNodes().count(), is(4L));

    // Check message flows
    assertThat(result.getMessageFlows().size(), is(3));
    Set<String> messageFlowNames =
        result.getMessageFlows().stream()
            .map(MessageFlow::getNameOrDescriptiveName)
            .collect(Collectors.toSet());
    assertThat(
        messageFlowNames,
        is(Sets.newHashSet("sendEvent_startP2", "SendTask_receiveEvent", "endP1_ReceiveTask")));
  }

  @Test
  void readContainedSubprocess() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "subprocesses.bpmn");

    // Expect the model shown here: https://cawemo.com/share/f26bcf22-fbb8-4cf7-8fd9-1b9004163e8d
    assertNotNull(result);
    assertThat(result.getName(), is("subprocesses"));
    assertThat(result.getParticipants().size(), is(1));
    BPMNProcess participant = result.getParticipants().iterator().next();
    assertThat(participant.getName(), is("subprocesses"));

    assertThat(participant.flowNodes().count(), is(3L));
    // Check sequence flows
    Set<String> sFDescriptiveNames = getSequenceFlowDescriptiveNamesForProcess(participant);
    assertThat(
        sFDescriptiveNames,
        is(Sets.newHashSet("start -> Sub1 (Flow_03uttu8)", "Sub1 -> end (Flow_0owdnmu)")));

    // Check Sub1 subprocess
    Map<String, FlowNode> flowNodesSub1 = createFlowNodeNameToFlowNodeMap(participant);
    CallActivity sub1 = getCallActivityForName(flowNodesSub1, "Sub1");
    // Check sequence flows for Sub1
    Set<String> sequenceFlowNamesSub1 =
        getSequenceFlowDescriptiveNamesForProcess(sub1.getSubProcessModel());
    assertThat(
        sequenceFlowNamesSub1,
        is(
            Sets.newHashSet(
                "start_sub1 -> Sub2 (Flow_1mo7qyz)", "Sub2 -> end_sub1 (Flow_1kq2gg6)")));

    // Check Sub2 subprocess
    Map<String, FlowNode> flowNodesSub2 =
        createFlowNodeNameToFlowNodeMap(sub1.getSubProcessModel());
    CallActivity sub2 = getCallActivityForName(flowNodesSub2, "Sub2");
    // Check sequence flows for Sub2
    Set<String> sequenceFlowNamesSub2 =
        getSequenceFlowDescriptiveNamesForProcess(sub2.getSubProcessModel());
    assertThat(
        sequenceFlowNamesSub2,
        is(
            Sets.newHashSet(
                "start_sub2 -> Sub3 (Flow_0f00dmj)", "Sub3 -> end_sub2 (Flow_1hxg3hk)")));

    // Check Sub3 subprocess
    Map<String, FlowNode> flowNodesSub3 =
        createFlowNodeNameToFlowNodeMap(sub2.getSubProcessModel());
    CallActivity sub3 = getCallActivityForName(flowNodesSub3, "Sub3");
    // Check sequence flows for Sub3
    Set<String> sequenceFlowNamesSub3 =
        getSequenceFlowDescriptiveNamesForProcess(sub3.getSubProcessModel());
    assertThat(sequenceFlowNamesSub3, is(Sets.newHashSet("start_sub3 -> end_sub3 (Flow_1qc94ba)")));
  }

  @Test
  void readEventSubprocess() throws IOException {
    BPMNCollaboration result = readModelFromResource(READER_TEST_PATH + "event-subprocesses.bpmn");

    // Expect the model shown here: https://cawemo.com/share/1d7d7d68-c480-45f1-85e4-ed579c944295
    assertNotNull(result);
    assertThat(result.getName(), is("event-subprocesses"));
    assertThat(result.getParticipants().size(), is(1));
    BPMNProcess participant = result.getParticipants().iterator().next();
    assertThat(participant.getName(), is("process"));

    assertThat(participant.flowNodes().count(), is(0L));
    assertThat(participant.eventSubprocesses().count(), is(1L));

    @SuppressWarnings("OptionalGetWithoutIsPresent") // Count is 1 means exactly one is present.
    BPMNEventSubprocess eventSubprocess1 = participant.eventSubprocesses().findFirst().get();
    assertThat(eventSubprocess1.getName(), is("Event subprocess1"));
    assertThat(eventSubprocess1.flowNodes().count(), is(14L));
    assertThat(eventSubprocess1.sequenceFlows().count(), is(7L));

    Set<StartEvent> startEvents = eventSubprocess1.getStartEvents();
    assertThat(startEvents.size(), is(7));

    String signalNonStartName = "signalNon";
    String signalStartName = "signal";
    String escNonStartName = "escNon";
    String escStartName = "esc";
    String errorStartName = "error";
    assertThat(
        startEvents,
        is(
            Sets.newHashSet(
                new StartEvent(
                    "Event_0ylomzh",
                    "msgNon",
                    StartEventType.MESSAGE,
                    EventDefinition.empty(),
                    false),
                new StartEvent("Event_1jhx76i", "msg", StartEventType.MESSAGE),
                new StartEvent(
                    "Event_0two4fk",
                    signalNonStartName,
                    StartEventType.SIGNAL,
                    new SignalEventDefinition(signalNonStartName),
                    false),
                new StartEvent(
                    "Event_1dxg1zq",
                    signalStartName,
                    StartEventType.SIGNAL,
                    new SignalEventDefinition(signalStartName),
                    true),
                new StartEvent(
                    "Event_19zuytf",
                    escNonStartName,
                    StartEventType.ESCALATION,
                    new SignalEventDefinition(escNonStartName),
                    false),
                new StartEvent(
                    "Event_16jr11v",
                    escStartName,
                    StartEventType.ESCALATION,
                    new SignalEventDefinition(escStartName),
                    true),
                new StartEvent(
                    "Event_0kibv8n",
                    errorStartName,
                    StartEventType.ERROR,
                    new SignalEventDefinition(errorStartName),
                    true))));

    // Check event subprocess inside event subprocess
    assertThat(eventSubprocess1.eventSubprocesses().count(), is(1L));
    @SuppressWarnings("OptionalGetWithoutIsPresent") // Count is 1 means exactly one is present.
    BPMNEventSubprocess eventSubprocess2 = eventSubprocess1.eventSubprocesses().findFirst().get();
    assertThat(eventSubprocess2.getName(), is("Event subprocess2"));
    assertThat(eventSubprocess2.flowNodes().count(), is(2L));
    assertThat(eventSubprocess2.sequenceFlows().count(), is(1L));
  }

  @Test
  void readFromStream() throws Exception {
    String resourcePath = READER_TEST_PATH + "pools-message-flows.bpmn";
    @SuppressWarnings("ConstantConditions")
    Path model = FileTestHelper.getResource(resourcePath);
    BPMNFileReader bpmnFileReader = new BPMNFileReader();
    BPMNCollaboration bpmnCollaboration =
        bpmnFileReader.readModelFromStream(Files.newInputStream(model));

    assertNotNull(bpmnCollaboration);
    assertThat(bpmnCollaboration.getParticipants().size(), is(2));
    // Rest is checked in the real testcase above.
  }

  @Test
  void readWithElementNameTransformer() throws IOException {
    BPMNCollaboration result =
        readModelFromResource(
            READER_TEST_PATH + "tasks.bpmn",
            (name) -> {
              if (name.equals("start")) {
                return "startNameChanged";
              }
              return name;
            });

    // Expect the model shown here: https://cawemo.com/share/882d7c5b-bff0-4244-a39f-a234795035e5
    BPMNProcess participant = result.getParticipants().iterator().next();
    // Sequence flows between the right flow nodes. Now with an updated name!
    Set<String> sFDescriptiveNames = getSequenceFlowDescriptiveNamesForProcess(participant);
    assertThat(
        sFDescriptiveNames,
        is(
            Sets.newHashSet(
                "startNameChanged -> task (Flow_08k9gv1)",
                "task -> sendTask (Flow_1a1i07n)",
                "sendTask -> receiveTask (Flow_0zbc6ny)",
                "receiveTask -> userTask (Flow_1p412ok)",
                "userTask -> manualTask (Flow_0u1hqoj)",
                "manualTask -> businessRTask (Flow_1my2hks)",
                "businessRTask -> serviceTask (Flow_1favftk)",
                "serviceTask -> scriptTask (Flow_1d05hhp)",
                "scriptTask -> subprocess (Flow_16xvne2)",
                "subprocess -> end (Flow_1kb5ejz)")));
  }

  private CallActivity getCallActivityForName(Map<String, FlowNode> flowNodes, String name) {
    FlowNode subprocess = flowNodes.get(name);
    if (subprocess instanceof CallActivity) {
      return (CallActivity) subprocess;
    }
    throw new RuntimeException(
        String.format(
            "Expected a call activity with name \"%s\" but it was not a call activity or null!",
            name));
  }

  private Map<String, FlowNode> createFlowNodeNameToFlowNodeMap(BPMNProcess participant) {
    return participant
        .flowNodes()
        .collect(Collectors.toMap(FlowNode::getName, Function.identity()));
  }
}
