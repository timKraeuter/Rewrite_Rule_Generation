package no.tk.maude.generation;

import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.CONFIGURATION;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.MESSAGES;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;

/** A special smart Maude rule builder to build BPMN rules for a given collaboration. */
public class BPMNMaudeRuleBuilder extends MaudeRuleBuilderBase<BPMNMaudeRuleBuilder> {
  private final MaudeObjectBuilder objectBuilder;
  private final BPMNCollaboration collaboration;
  private final Set<String> consumedMessages;
  private final Set<String> createdMessages;

  private Set<String> signalAll;
  private final MaudeBPMNGenerationSettings settings;

  public BPMNMaudeRuleBuilder(
      BPMNCollaboration collaboration, MaudeBPMNGenerationSettings settings) {
    super();
    this.settings = settings;
    setSelfReference(this);
    this.collaboration = collaboration;
    consumedMessages = new LinkedHashSet<>();
    createdMessages = new LinkedHashSet<>();
    objectBuilder = new MaudeObjectBuilder();
    signalAll = new LinkedHashSet<>();
  }

  @Override
  public MaudeRule buildRule() {
    if (ruleName == null || preObjects.isEmpty() || postObjects.isEmpty()) {
      throw new MaudeGenerationException(
          "A rule should have a name and at least one pre/post object");
    }
    if (consumedMessages.isEmpty()
        && createdMessages.isEmpty()
        && signalAll.isEmpty()
        && settings.isPersistentMessages()) {
      return buildLocalProcessRule();
    }
    return buildGlobalSystemRule();
  }

  public void addMessageConsumption(String message) {
    this.consumedMessages.add(message);
  }

  public void addMessageCreation(String message) {
    this.createdMessages.add(message);
  }

  public void addSignalAll(Set<String> signalAll) {
    this.addVar("other processes", CONFIGURATION, PS);
    this.signalAll = signalAll;
  }

  private MaudeRule buildGlobalSystemRule() {
    if (signalAll != null && !signalAll.isEmpty()) {
      return buildSystemRuleWithSignalAll();
    }
    // Pre object is a BPMN system
    MaudeObject preObject =
        createBPMNSystem(
            getObjectStringAndAddAnyOtherProcesses(preObjects),
            getMessagesString(consumedMessages));

    // Post object is a BPMN system
    MaudeObject postObject =
        createBPMNSystem(
            getObjectStringAndAddAnyOtherProcesses(postObjects),
            getCreatedMessagesString(createdMessages));
    return createRuleAndResetBuilder(
        Collections.singleton(preObject), Collections.singleton(postObject));
  }

  private MaudeRule createRuleAndResetBuilder(
      Set<MaudeObject> preObject, Set<MaudeObject> postObject) {
    MaudeRule maudeRule = new MaudeRule(ruleName, preObject, postObject, condition);
    createdRules.add(maudeRule);
    this.reset();
    return maudeRule;
  }

  private MaudeRule buildSystemRuleWithSignalAll() {
    if (preObjects.size() != 1) {
      throw new MaudeGenerationException(
          "There must be exactly one pre object in signal throw rules!");
    }
    // Pre object is a BPMN system
    MaudeObject preObject =
        createBPMNSystem(
            getObjectString(preObjects) + WHITE_SPACE + PS, getMessagesString(consumedMessages));

    String postProcesses =
        String.format(
            "signalAll(%s %s, %s)",
            getObjectString(postObjects), PS, String.join(WHITE_SPACE, signalAll));
    // Post object is a BPMN system
    MaudeObject postObject =
        createBPMNSystem(postProcesses, getCreatedMessagesString(createdMessages));

    return createRuleAndResetBuilder(
        Collections.singleton(preObject), Collections.singleton(postObject));
  }

  private String getCreatedMessagesString(Set<String> createdMessages) {
    if (settings.isPersistentMessages()) {
      return getMessagesString(createdMessages);
    }
    // Delete messages besides the created one since messages are set to non-persistent.
    if (createdMessages.isEmpty()) {
      return NONE;
    }
    return String.join(WHITE_SPACE, createdMessages);
  }

  private String getObjectStringAndAddAnyOtherProcesses(Set<MaudeObject> objects) {
    return getObjectString(objects) + ANY_OTHER_PROCESSES;
  }

  private String getObjectString(Set<MaudeObject> objects) {
    return objects.stream()
        .map(MaudeObject::generateObjectString)
        .collect(Collectors.joining(WHITE_SPACE));
  }

  private String getMessagesString(Set<String> messages) {
    if (messages.isEmpty()) {
      return ANY_MESSAGES;
    }
    return String.join(WHITE_SPACE, messages) + ANY_OTHER_MESSAGES;
  }

  public MaudeObject createBPMNSystem(String processes, String messages) {
    String name =
        collaboration.getName().isBlank() ? "unnamedCollaboration" : collaboration.getName();
    return objectBuilder
        .oid(String.format(ENQUOTE_FORMAT, name))
        .oidType(BPMN_SYSTEM)
        .addAttributeValue(MESSAGES, String.format(BRACKET_FORMAT, messages))
        .addAttributeValue(PROCESSES, String.format(BRACKET_FORMAT, processes))
        .build();
  }

  private MaudeRule buildLocalProcessRule() {
    return createRuleAndResetBuilder(preObjects, postObjects);
  }

  @Override
  void reset() {
    this.ruleName = null;
    this.preObjects = new LinkedHashSet<>();
    this.postObjects = new LinkedHashSet<>();
    consumedMessages.clear();
    createdMessages.clear();
    signalAll = new LinkedHashSet<>();
  }
}
