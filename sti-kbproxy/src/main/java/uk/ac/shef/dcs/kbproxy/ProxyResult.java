package uk.ac.shef.dcs.kbproxy;

import java.util.List;

/**
 * Result of proxy operations.
 * 
 * @author Jan Váňa
 */
public final class ProxyResult<T> {
  
  private final T result;
  private final String warning;

  public ProxyResult(final T result) {
    this.result = result;
    this.warning = null;
  }
  
  public ProxyResult(final T result, final String warning) {
    this.result = result;
    this.warning = warning;
  }

  public void appendExistingWarning(final List<String> warnings) {
    if (this.warning != null) {
      warnings.add(this.warning);
    }
  }

  public T getResult() {
    return this.result;
  }

  public String getWarning() {
    return this.warning;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ProxyResult [result=" + result + ", warning=" + warning + "]";
  }
}
