package uk.ac.shef.dcs.sti.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.util.Asserts;

import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.Pair;

/**
 * Created by JanVa_000 on 09.02.2017.
 */
public class DisambiguationResult {
  private List<Pair<Entity, Map<String, Double>>> result;
  private List<String> warnings;

  public DisambiguationResult(final List<Pair<Entity, Map<String, Double>>> result) {
    Asserts.notNull(result, "Disambiguation result");

    this.result = result;
    this.warnings = new ArrayList<>();
  }

  public DisambiguationResult(final List<Pair<Entity, Map<String, Double>>> result,
      final List<String> warnings) {
    Asserts.notNull(result, "Disambiguation result");
    Asserts.notNull(result, "Disambiguation warnings");

    this.result = result;
    this.warnings = warnings;
  }

  public List<Pair<Entity, Map<String, Double>>> getResult() {
    return this.result;
  }

  public List<String> getWarnings() {
    return this.warnings;
  }

  public void setResult(final List<Pair<Entity, Map<String, Double>>> result) {
    this.result = result;
  }

  public void setWarnings(final List<String> warnings) {
    this.warnings = warnings;
  }
}
