package uk.ac.shef.dcs.sti.core.model;

import org.apache.http.util.Asserts;

import uk.ac.shef.dcs.kbproxy.model.Entity;

import java.util.ArrayList;

/**
 * Created by JanVa_000 on 09.02.2017.
 */
public class EntityResult {
  private java.util.List<Entity> result;
  private java.util.List<String> warnings;

  public java.util.List<Entity> getResult() {
    return result;
  }

  public void setResult(java.util.List<Entity> result) {
    this.result = result;
  }

  public java.util.List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(java.util.List<String> warnings) {
    this.warnings = warnings;
  }

  public EntityResult(java.util.List<Entity> result) {
    Asserts.notNull(result, "Entity result");

    this.result = result;
    this.warnings = new ArrayList<>();
  }

  public EntityResult(java.util.List<Entity> result, java.util.List<String> warnings) {
    Asserts.notNull(result, "Entity result");
    Asserts.notNull(result, "Entity warnings");

    this.result = result;
    this.warnings = warnings;
  }
}
