package groove.behaviorTransformer;

import behavior.Behavior;
import groove.GxlToXMLConverter;
import groove.graph.GrooveGraph;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleWriter;
import groove.gxl.Gxl;

import java.io.File;
import java.util.stream.Stream;

import static groove.behaviorTransformer.BehaviorToGrooveTransformer.START_GST;

public interface GrooveTransformer<SOURCE extends Behavior> {
    // Special groove labels
    String AT = "@";
    String FORALL = "forall:";
    String EXISTS_OPTIONAL = "existsx:";
    // Nesting of quantifiers
    String IN = "in";

    String BOOL = "bool:";
    String FALSE = BOOL + "false";
    String TRUE = BOOL + "true";
    String BOOL_NOT = "bool:not";
    String BOOL_AND = "bool:and";
    String BOOL_OR = "bool:or";
    String UNEQUALS = "!=";
    String INT = "int:";
    String ARG_0 = "arg:0";
    String ARG_1 = "arg:1";
    String INT_ADD = "int:add";
    String INT_SUB = "int:sub";
    String PROD = "prod:";
    String INT_LT = "int:lt";
    String INT_LE = "int:le";
    String INT_EQ = "int:eq";
    String INT_GE = "int:ge";
    String INT_GT = "int:gt";

    String TYPE = "type:";
    String STRING = "string:";

    static void writeStartGraph(File targetFolder, GrooveGraph startGraph, boolean layoutActivated) {
        Gxl gxl = BehaviorToGrooveTransformer.createGxlFromGrooveGraph(startGraph, layoutActivated);
        File startGraphFile = new File(targetFolder.getPath() + START_GST);

        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    GrooveGraph generateStartGraph(SOURCE source, boolean addPrefix);

    default void generateAndWriteStartGraph(SOURCE source, boolean addPrefix, File targetFolder) {
        GrooveGraph startGraph = this.generateStartGraph(source, addPrefix);
        writeStartGraph(targetFolder, startGraph, this.isLayoutActivated());
    }

    Stream<GrooveGraphRule> generateRules(SOURCE source, boolean addPrefix);

    default void generateAndWriteRules(SOURCE source, boolean addPrefix, File targetFolder) {
        Stream<GrooveGraphRule> rules = this.generateRules(source, addPrefix);
        GrooveRuleWriter.writeRules(rules, targetFolder);
        this.generateAndWriteRulesFurther(source, addPrefix, targetFolder);
    }

    default void generateAndWriteRulesFurther(SOURCE source, boolean addPrefix, File targetFolder) {
        // to be overridden if needed
    }

    boolean isLayoutActivated();
}