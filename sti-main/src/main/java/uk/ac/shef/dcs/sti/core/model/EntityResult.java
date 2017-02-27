package uk.ac.shef.dcs.sti.core.model;

import java.util.ArrayList;

import org.apache.http.util.Asserts;

import uk.ac.shef.dcs.kbproxy.model.Entity;

/**
 * Created by JanVa_000 on 09.02.2017.
 */
public class EntityResult {
  private java.util.List<Entity> result;
  private java.util.List<String> warnings;

  public EntityResult(final java.util.List<Entity> result) {
    Asserts.notNull(result, "Entity result");

    this.result = result;
    this.warnings = new ArrayList<>();
  }

  public EntityResult(final java.util.List<Entity> result, final java.util.List<String> warnings) {
    Asserts.notNull(result, "Entity result");
    Asserts.notNull(result, "Entity warnings");

    this.result = result;
    this.warnings = warnings;
  }

  public java.util.List<Entity> getResult() {
    return this.result;
  }

  public java.util.List<String> getWarnings() {
    return this.warnings;
  }

  public void setResult(final java.util.List<Entity> result) {
    this.result = result;
  }

  public void setWarnings(final java.util.List<String> warnings) {
    this.warnings = warnings;
  }
}
