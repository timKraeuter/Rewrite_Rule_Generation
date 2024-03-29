package no.tk.groove.behaviortransformer;

import io.github.timkraeuter.groove.graph.GrooveGraphBuilder;
import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.behavior.picalculus.*;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PiCalcToGrooveTransformer extends GrooveTransformer<NamedPiProcess> {

  // Possible node labels.
  private static final String TYPE_PROCESS = TYPE + "Process";
  private static final String TYPE_OUT = TYPE + "Out";
  private static final String TYPE_IN = TYPE + "In";
  private static final String TYPE_SUMMATION = TYPE + "Summation";
  private static final String TYPE_COERCION = TYPE + "Coercion";
  private static final String TYPE_NAME = TYPE + "Name";
  private static final String TYPE_SUM = TYPE + "Sum";
  private static final String TYPE_PAR = TYPE + "Par";
  private static final String TYPE_RESTRICTION = TYPE + "Restriction";

  // Possible edge labels
  private static final String PROCESS = "process";
  private static final String OP = "op";
  private static final String C = "c";
  private static final String CHANNEL = "channel";
  private static final String PAYLOAD = "payload";
  private static final String PAR = "par";
  private static final String RES = "res";
  private static final String SUM = "sum";
  private static final String ARG_1 = "arg1";
  private static final String ARG_2 = "arg2";
  private static final String NAME = "name";

  private GrooveGraphBuilder graphBuilder;
  private Map<String, GrooveNode> nameToNode;

  public PiCalcToGrooveTransformer(boolean layout) {
    super(layout);
  }

  @Override
  public void generateRules(NamedPiProcess namedPiProcess, GrooveRuleBuilder rules) {
    // Fixed set of rules for Pi. We do not think about synchronisation yet.
  }

  @Override
  public void generateAndWriteRulesFurther(NamedPiProcess namedPiProcess, Path targetFolder) {
    this.copyPiRulesAndTypeGraph(targetFolder);
  }

  void copyPiRulesAndTypeGraph(Path targetFolder) {
    try {
      Path sourceDirectory = Paths.get(this.getClass().getResource("/GaducciPi").toURI());
      PathUtils.copyDirectory(sourceDirectory, targetFolder, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException | URISyntaxException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
  }

  @Override
  public void generateStartGraph(NamedPiProcess piProcess, GrooveGraphBuilder builder) {
    // Prefixing is not needed for the pi-calculus since a shared set of rules is used for
    // everything.

    this.graphBuilder = builder.name(piProcess.getName());
    this.nameToNode = new LinkedHashMap<>();

    // Create root process with flag go.
    GrooveNode rootNode = this.createAndSaveNodeWithName(TYPE_PROCESS);
    rootNode.addFlag("go");

    Optional<Pair<GrooveNode, String>> parOrSum = this.convertProcess(piProcess.getProcess(), true);
    parOrSum.ifPresent(
        nodeEdgelabelPair ->
            this.createAndSaveEdgeWithName(
                nodeEdgelabelPair.getRight(), rootNode, nodeEdgelabelPair.getLeft()));
  }

  private Optional<Pair<GrooveNode, String>> convertProcess(
      PiProcess piProcess, boolean addCoercion) {
    return piProcess.accept(
        new PiProcessVisitor<>() {

          @Override
          public Optional<Pair<GrooveNode, String>> handle(Parallelism parallelism) {
            GrooveNode parNode = PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_PAR);

            // arg 1
            GrooveNode processNodeArg1 =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_PROCESS);
            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                ARG_1, parNode, processNodeArg1);

            final PiProcess arg1 = parallelism.getFirst();
            Optional<Pair<GrooveNode, String>> arg1Continuation =
                PiCalcToGrooveTransformer.this.convertProcess(arg1, true);
            arg1Continuation.ifPresent(
                nodeEdgeLabelPair ->
                    PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                        nodeEdgeLabelPair.getRight(),
                        processNodeArg1,
                        nodeEdgeLabelPair.getLeft()));

            // arg2
            GrooveNode processNodeArg2 =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_PROCESS);
            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                ARG_2, parNode, processNodeArg2);

            final PiProcess arg2 = parallelism.getSecond();
            Optional<Pair<GrooveNode, String>> arg2Continuation =
                PiCalcToGrooveTransformer.this.convertProcess(arg2, true);
            arg2Continuation.ifPresent(
                nodeEdgeLabelPair ->
                    PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                        nodeEdgeLabelPair.getRight(),
                        processNodeArg2,
                        nodeEdgeLabelPair.getLeft()));

            return Optional.of(Pair.of(parNode, PAR));
          }

          @Override
          public Optional<Pair<GrooveNode, String>> handle(NameRestriction restriction) {
            // TODO: Add picking a free name and renaming!
            final GrooveNode resNode =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_RESTRICTION);

            // Create restricted name
            GrooveNode nameNode = this.createNodeForNameIfNeeded(restriction.getRestrictedName());
            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(NAME, resNode, nameNode);

            // Create process for subprocess
            GrooveNode processNode =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_PROCESS);
            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(PROCESS, resNode, processNode);

            // Connect subprocess
            Optional<Pair<GrooveNode, String>> subProcessNode =
                PiCalcToGrooveTransformer.this.convertProcess(
                    restriction.getRestrictedProcess(), true);
            subProcessNode.ifPresent(
                nodeEdgeLabelPair ->
                    PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                        nodeEdgeLabelPair.getRight(), processNode, nodeEdgeLabelPair.getLeft()));

            return Optional.of(Pair.of(resNode, RES));
          }

          @Override
          public Optional<Pair<GrooveNode, String>> handle(PrefixedProcess prefixedProcess) {
            // TODO: Add picking a free name and renaming!
            GrooveNode topLevelNode;

            GrooveNode summationNode =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_SUMMATION);

            topLevelNode = summationNode;
            if (addCoercion) {
              GrooveNode coercionNode =
                  PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_COERCION);
              PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                  C, coercionNode, summationNode);
              topLevelNode = coercionNode;
            }

            GrooveNode opNode =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(
                    this.getPrefixTypeString(prefixedProcess.getPrefixType()));

            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(OP, summationNode, opNode);

            GrooveNode channelNode = this.createNodeForNameIfNeeded(prefixedProcess.getChannel());
            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(CHANNEL, opNode, channelNode);

            GrooveNode payloadNode = this.createNodeForNameIfNeeded(prefixedProcess.getPayload());
            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(PAYLOAD, opNode, payloadNode);

            GrooveNode processNode =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_PROCESS);
            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(PROCESS, opNode, processNode);

            Optional<Pair<GrooveNode, String>> subProcessNode =
                PiCalcToGrooveTransformer.this.convertProcess(prefixedProcess.getProcess(), true);
            subProcessNode.ifPresent(
                nodeEdgeLabelPair ->
                    PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                        nodeEdgeLabelPair.getRight(), processNode, nodeEdgeLabelPair.getLeft()));

            return Optional.of(Pair.of(topLevelNode, addCoercion ? C : ""));
          }

          private GrooveNode createNodeForNameIfNeeded(String name) {
            GrooveNode nameNode = PiCalcToGrooveTransformer.this.nameToNode.get(name);
            if (nameNode == null) {
              nameNode = PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_NAME);
              // TODO: add pi-name to the node somehow!
              PiCalcToGrooveTransformer.this.nameToNode.put(name, nameNode);
            }
            return nameNode;
          }

          private String getPrefixTypeString(PrefixType prefixType) {
            return switch (prefixType) {
              case OUT -> TYPE_OUT;
              case IN -> TYPE_IN;
            };
          }

          @Override
          public Optional<Pair<GrooveNode, String>> handle(EmptySum emptySum) {
            return Optional.empty();
          }

          @Override
          public Optional<Pair<GrooveNode, String>> handle(MultiarySum multiarySum) {
            GrooveNode topLevelNode;
            GrooveNode summationNode =
                PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_SUMMATION);

            topLevelNode = summationNode;
            if (addCoercion) {
              GrooveNode coercionNode =
                  PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_COERCION);
              PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                  C, coercionNode, summationNode);
              topLevelNode = coercionNode;
            }
            GrooveNode sumNode = PiCalcToGrooveTransformer.this.createAndSaveNodeWithName(TYPE_SUM);

            PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(SUM, summationNode, sumNode);

            // Arg 1
            Sum first = multiarySum.getFirst();
            Optional<Pair<GrooveNode, String>> arg1 =
                PiCalcToGrooveTransformer.this.convertProcess(first, false);
            arg1.ifPresent(
                nodeStringPair ->
                    PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                        ARG_1, sumNode, nodeStringPair.getLeft()));

            // Arg 2
            Sum second = multiarySum.getSecond();
            Optional<Pair<GrooveNode, String>> arg2 =
                PiCalcToGrooveTransformer.this.convertProcess(second, false);
            arg2.ifPresent(
                nodeStringPair ->
                    PiCalcToGrooveTransformer.this.createAndSaveEdgeWithName(
                        ARG_2, sumNode, nodeStringPair.getLeft()));

            return Optional.of(Pair.of(topLevelNode, addCoercion ? C : ""));
          }
        });
  }

  private void createAndSaveEdgeWithName(
      String name, GrooveNode sourceNode, GrooveNode targetNode) {
    this.graphBuilder.addEdge(name, sourceNode, targetNode);
  }

  private GrooveNode createAndSaveNodeWithName(String name) {
    GrooveNode grooveNode = new GrooveNode(name);
    this.graphBuilder.addNode(grooveNode);
    return grooveNode;
  }
}
