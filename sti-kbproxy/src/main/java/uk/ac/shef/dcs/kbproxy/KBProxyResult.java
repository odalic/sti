package uk.ac.shef.dcs.kbproxy;

import com.google.common.base.Defaults;

import java.util.List;

/**
 * Result of KBProxy operations
 * Created by JanVa_000 on 06.02.2017.
 */
public class KBProxyResult<ResultType> {
  private ResultType result;
  private String warning;

  public ResultType getResult() {
    return result;
  }

  private void setResult(ResultType result) {
    this.result = result;
  }

  public String getWarning() {
    return warning;
  }

  private void setWarning(String warning) {
    this.warning = warning;
  }

  public KBProxyResult(ResultType result) {
    setResult(result);
  }

  public KBProxyResult(ResultType result, String warning) {
    this(result);
    setWarning(warning);
  }

  public void appendWarning(List<String> warnings) {
    if (warning != null) {
      warnings.add(warning);
    }
  }
}
