package no.tk.maude.behaviortransformer.bpmn;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class BPMNToMaudeEventsTest implements BPMNMaudeTestHelper {

  /**
   * See test case <a href="https://cawemo.com/share/b115e18b-0136-41a7-940a-8190c97da07e">"Multiple
   * Start Events"</a> in cawemo.
   */
  @Test
  void testMultipleStartEvents() throws IOException {
    testBPMNMaudeGeneration("multiple-start-events");
  }

  /**
   * See test case <a href="https://cawemo.com/share/e6a2eb93-b0e7-4c09-baa0-93ff18084d0e">"Message
   * Events"</a> in cawemo.
   */
  @Test
  void testMessageEvents() throws IOException {
    testBPMNMaudeGeneration("message-events");
  }

  /**
   * See test case <a href="https://cawemo.com/share/b4b588fa-3f0f-4c30-95e9-b7f5dd40cd7a">"Message
   * Events without Message Flows"</a> in cawemo.
   */
  @Test
  void testMessageEventsNoMessageFlows() throws IOException {
    testBPMNMaudeGeneration("message-events-without-message-flows");
  }

  /**
   * See test case <a href="https://cawemo.com/share/44d74e7b-f940-48cd-8ceb-d23976b4da2b">"Two
   * Incoming Message Flows"</a> in cawemo.
   */
  @Test
  void testTwoIncomingMessageFlows() throws IOException {
    testBPMNMaudeGeneration("two-incoming-message-flows");
  }

  /**
   * See test case <a href="https://cawemo.com/share/e1777355-d0cc-45d0-8f01-87d08ba2b5ef">"Two End
   * Events"</a> in cawemo.
   */
  @Test
  void testEndEvents() throws IOException {
    testBPMNMaudeGeneration("end-events");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/e579995b-65f3-4146-a974-f136f5fd949b">"Terminate End Event"</a>
   * in cawemo.
   */
  @Test
  void testTerminateEndEvent() throws IOException {
    testBPMNMaudeGeneration("terminate-end-event");
  }

  /**
   * See test case <a href="https://cawemo.com/share/519f49aa-e3ec-4d6d-8425-3933f93f974d">"Link
   * Event"</a> in cawemo.
   */
  @Test
  void testLinkEvent() throws IOException {
    testBPMNMaudeGeneration("link-event");
  }

  /**
   * See test case <a href="https://cawemo.com/share/e13f777e-dca2-45e9-8018-0b9d0c4b34cc">"Signal
   * Events"</a> in cawemo.
   */
  @Test
  void testSignalEvents() throws IOException {
    testBPMNMaudeGeneration("signal-events", CAN_TERMINATE_QUERY);
  }

  /**
   * See test case <a href="https://cawemo.com/share/6a29e7e5-bf10-4b3e-bb40-2ff8591f7e0c">"Signal
   * Events - Multi Activation"</a> in cawemo.
   */
  @Test
  void testSignalEventsMultiActivation() throws IOException {
    testBPMNMaudeGeneration("signal-events-multi-activation", CAN_TERMINATE_QUERY);
  }

  /**
   * See test case <a href="https://cawemo.com/share/813dee70-ddc2-4a71-a965-1b6a2d28c7fa">"Signal
   * events - Multi Activation - Same Process"</a> in cawemo.
   */
  @Test
  void testSignalEventsMultiActivationSameProcess() throws IOException {
    testBPMNMaudeGeneration("signal-events-multi-activation-same-process", CAN_TERMINATE_QUERY);
  }

  /**
   * See test case <a href="https://cawemo.com/share/15e29a24-e35b-40b8-b09a-b63f3bc91268">"Timer
   * events"</a> in cawemo.
   */
  @Test
  void testTimerEvents() throws IOException {
    testBPMNMaudeGeneration("timer-events");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/0b3cb831-a6b2-4e7c-b064-0c83e887bf47">"Intermediate Throw
   * Event"</a> in cawemo.
   */
  @Test
  void testIntermediateThrowEvent() throws IOException {
    testBPMNMaudeGeneration("intermediate-throw-event");
  }
}
