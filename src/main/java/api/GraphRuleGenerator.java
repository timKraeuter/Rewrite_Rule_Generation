package api;

public interface GraphRuleGenerator {

    void startRule(String ruleName);

    /**
     * Definte that the current rule needs a node in the context with the given name.
     */
    Node contextNode(String name);

    /**
     * Define that the current rule adds a node with the given name.
     */
    Node addNode(String nodeName);

    /**
     * Define that the current rule adds an edge between the two given nodes.
     */
    void addEdge(String name, Node source, Node target);

    /**
     * Define that the current rule deletes a node with the given name.
     */
    Node deleteNode(String nodeName);

    /**
     * Define that the current rule deletes an edge between two nodes (The nodes must be in context, added or deleted).
     */
    void deleteEdge(String name, Node source, Node target);

    void generateRule();
}
