package uk.ac.shef.dcs.kbproxy;

import java.util.List;

/**
 * Result of KBProxy operations Created by JanVa_000 on 06.02.2017.
 */
public class KBProxyResult<ResultType> {
  private ResultType result;
  private String warning;

  public KBProxyResult(final ResultType result) {
    setResult(result);
  }

  public KBProxyResult(final ResultType result, final String warning) {
    this(result);
    setWarning(warning);
  }

  public void appendWarning(final List<String> warnings) {
    if (this.warning != null) {
      warnings.add(this.warning);
    }
  }

  public ResultType getResult() {
    return this.result;
  }

  public String getWarning() {
    return this.warning;
  }

  private void setResult(final ResultType result) {
    this.result = result;
  }

  private void setWarning(final String warning) {
    this.warning = warning;
  }
}
