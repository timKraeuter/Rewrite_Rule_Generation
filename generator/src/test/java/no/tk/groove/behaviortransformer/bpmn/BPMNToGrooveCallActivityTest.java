package no.tk.groove.behaviortransformer.bpmn;

import org.junit.jupiter.api.Test;

class BPMNToGrooveCallActivityTest extends BPMNToGrooveTestBase {

  /**
   * See test case <a href="https://cawemo.com/share/be4afe81-4adf-4b45-a933-5745316ee533">"Call
   * activity - Simple"</a> in cawemo.
   */
  @Test
  void testCallActivity() throws Exception {
    testGrooveGenerationForBPMNResourceFile("call-activity-simple.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/321db543-e301-4bfd-ac04-e6618346433a">"Call
   * activity - Implicit exclusive and parallel gateway"</a> in cawemo.
   */
  @Test
  void testCallActivityImplicitGateways() throws Exception {
    testGrooveGenerationForBPMNResourceFile(
        "call-activity-implicit-exclusive-and-parallel-gateway.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/63333b2a-8298-4e9c-8e1e-14453ed1f063">"Call
   * activity - Terminate end event"</a> in cawemo.
   */
  @Test
  void testCallActivityTerminateEvent() throws Exception {
    testGrooveGenerationForBPMNResourceFile("call-activity-terminate-end-event.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/37764ed1-03e7-43c7-8218-34467a12d104">"Call
   * activity - No start event"</a> in cawemo.
   */
  @Test
  void testCallActivityNoStartEvent() throws Exception {
    testGrooveGenerationForBPMNResourceFile("call-activity-no-start-event.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/3b573dc4-3592-421d-9b13-dbb4e45ff410">"Call
   * activity - Complex"</a> in cawemo.
   */
  @Test
  void testCallActivityComplex() throws Exception {
    testGrooveGenerationForBPMNResourceFile("call-activity-complex.bpmn");
  }
}
