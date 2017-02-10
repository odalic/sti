package uk.ac.shef.dcs.sti.core.model;

import org.apache.http.util.Asserts;

import uk.ac.shef.dcs.kbproxy.model.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;

import javafx.util.Pair;

/**
 * Created by JanVa_000 on 09.02.2017.
 */
public class DisambiguationResult {
  private List<Pair<Entity, Map<String, Double>>> result;
  private List<String> warnings;

  public List<Pair<Entity, Map<String, Double>>> getResult() {
    return result;
  }

  public void setResult(List<Pair<Entity, Map<String, Double>>> result) {
    this.result = result;
  }

  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public DisambiguationResult(List<Pair<Entity, Map<String, Double>>> result) {
    Asserts.notNull(result, "Disambiguation result");

    this.result = result;
    this.warnings = new ArrayList<>();
  }

  public DisambiguationResult(List<Pair<Entity, Map<String, Double>>> result, List<String> warnings) {
    Asserts.notNull(result, "Disambiguation result");
    Asserts.notNull(result, "Disambiguation warnings");

    this.result = result;
    this.warnings = warnings;
  }
}
