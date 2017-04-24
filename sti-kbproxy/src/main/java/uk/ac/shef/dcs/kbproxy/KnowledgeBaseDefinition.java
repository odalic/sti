package uk.ac.shef.dcs.kbproxy;

import java.net.URI;

/**
 * Information about the knowledge base.
 *
 * @author Jan Váňa
 * @author Václav Brodec
 */
public class KnowledgeBaseDefinition {

  public static abstract class Builder<T extends Builder<T>> {

    private String name;  
    
    private boolean insertSupported;
    private URI insertPrefixData;
    private URI insertPrefixSchema;
    
    public KnowledgeBaseDefinition build() {
      return new KnowledgeBaseDefinition(this);
    }
    
    protected abstract T getThis();
    
    /**
     * @param name the name to set
     */
    public T setName(String name) {
      this.name = name;
      
      return getThis();
    }
    
    /**
     * @param insertSupported the insertSupported to set
     */
    public T setInsertSupported(boolean insertSupported) {
      this.insertSupported = insertSupported;

      return getThis();
    }

    /**
     * @param insertPrefixData the insertPrefixData to set
     */
    public T setInsertPrefixData(URI insertPrefixData) {
      this.insertPrefixData = insertPrefixData;

      return getThis();
    }
    
    /**
     * @param insertPrefixSchema the insertPrefixSchema to set
     */
    public T setInsertPrefixSchema(URI insertPrefixSchema) {
      this.insertPrefixSchema = insertPrefixSchema;

      return getThis();
    }
  }
  
  private final String name;
  
  private final boolean insertSupported;
  private final URI insertPrefixData;
  private final URI insertPrefixSchema;
  
  /**
   * @param name
   * @param insertSupported
   * @param insertPrefixData
   * @param insertPrefixSchema
   */
  protected KnowledgeBaseDefinition(final Builder<?> builder) {
    this.name = builder.name;
    this.insertSupported = builder.insertSupported;
    this.insertPrefixData = builder.insertPrefixData;
    this.insertPrefixSchema = builder.insertPrefixSchema;
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return the insertSupported
   */
  public boolean isInsertSupported() {
    return insertSupported;
  }
  
  /**
   * @return the insertPrefixData
   */
  public URI getInsertPrefixData() {
    return insertPrefixData;
  }
  
  /**
   * @return the insertPrefixSchema
   */
  public URI getInsertPrefixSchema() {
    return insertPrefixSchema;
  }
}
