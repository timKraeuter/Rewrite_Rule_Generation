package no.tk.groove.behaviortransformer.bpmn;

import org.junit.jupiter.api.Test;

class BPMNToGrooveTaskTest extends BPMNToGrooveTestBase {

  /**
   * See test case <a
   * href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Tasks"</a> in
   * cawemo.
   */
  @Test
  void testSequentialTasks() throws Exception {
    this.setFileNameFilter(x -> false); // Expect type graph here.
    testGrooveGenerationForBPMNResourceFile("sequential-tasks.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/9fdaa163-2b27-4787-99df-1ecf55971f14">"Implicit
   * Exclusive Gateway"</a> in cawemo.
   */
  @Test
  void testImplicitExclusiveGateway() throws Exception {
    testGrooveGenerationForBPMNResourceFile("implicit-exclusive-gateway.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/5e855137-d237-4bf7-bbf4-639c8e6093e0">"Implicit
   * Parallel Gateway"</a> in cawemo.
   */
  @Test
  void testImplicitParallelGateway() throws Exception {
    testGrooveGenerationForBPMNResourceFile("implicit-parallel-gateway.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/121dafdb-2ce5-4146-8f4e-315ab9bb0c38">"Send/Receive Message
   * Tasks"</a> in cawemo.
   */
  @Test
  void testSendReceiveTask() throws Exception {
    testGrooveGenerationForBPMNResourceFile("send-receive-message-tasks.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/e76c1763-4842-493c-bebb-cfa41e5abb09">"Instantiate Receive
   * Task"</a> in cawemo.
   */
  @Test
  void testInstantiateReceiveTask() throws Exception {
    testGrooveGenerationForBPMNResourceFile("instantiate-receive-task.bpmn");
  }
}
