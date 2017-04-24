package uk.ac.shef.dcs.kbproxy;

public interface Configurable<T extends KnowledgeBaseDefinition> {
  T getKbDefinition();
}
