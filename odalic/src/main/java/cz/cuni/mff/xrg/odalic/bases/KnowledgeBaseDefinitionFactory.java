package cz.cuni.mff.xrg.odalic.bases;

public interface KnowledgeBaseDefinitionFactory<T> {
  T create(KnowledgeBase base, Class<? extends T> type);
}
