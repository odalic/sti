package cz.cuni.mff.xrg.odalic.api.rdf.values;

import javax.annotation.Nullable;

import com.complexible.pinto.annotations.RdfProperty;
import com.complexible.pinto.annotations.RdfsClass;
import com.google.common.base.Preconditions;

@RdfsClass("http://odalic.eu/internal/AdvancedPropertyEntry")
public class AdvancedPropertyEntry {

  private String key;

  private String value;

  public AdvancedPropertyEntry() {}

  public AdvancedPropertyEntry(final String key,
      final String value) {
    Preconditions.checkNotNull(key, "The key cannot be null!");
    Preconditions.checkNotNull(value, "The value cannot be null!");

    this.key = key;
    this.value = value;
  }

  /**
   * @return the key
   */
  @RdfProperty("http://odalic.eu/internal/AdvancedPropertyEntry/key")
  @Nullable
  public String getKey() {
    return this.key;
  }

  /**
   * @return the value
   */
  @RdfProperty("http://odalic.eu/internal/AdvancedPropertyEntry/value")
  @Nullable
  public String getValue() {
    return this.value;
  }

  /**
   * @param key the key to set
   */
  public void setKey(final String key) {
    Preconditions.checkNotNull(key, "The key cannot be null!");

    this.key = key;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final String value) {
    Preconditions.checkNotNull(value, "The value cannot be null!");

    this.value = value;
  }

  @Override
  public String toString() {
    return "AdvancedPropertyEntry [key=" + this.key + ", value=" + this.value + "]";
  }
}
