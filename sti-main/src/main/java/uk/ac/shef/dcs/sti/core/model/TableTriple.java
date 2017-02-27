package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 */
public class TableTriple implements Serializable {

  private static final long serialVersionUID = -813672581122221313L;
  private String subject_annotation;
  private String subject;
  private int[] subject_position; // row, column of table
  private String object_annotation;
  private String object;
  private int[] object_position;
  private String relation_annotation;

  public String getObject() {
    return this.object;
  }

  public String getObject_annotation() {
    return this.object_annotation;
  }

  public int[] getObject_position() {
    return this.object_position;
  }

  public String getRelation_annotation() {
    return this.relation_annotation;
  }

  public String getSubject() {
    return this.subject;
  }

  public String getSubject_annotation() {
    return this.subject_annotation;
  }

  public int[] getSubject_position() {
    return this.subject_position;
  }

  public void setObject(final String object) {
    this.object = object;
  }

  public void setObject_annotation(final String object_annotation) {
    this.object_annotation = object_annotation;
  }

  public void setObject_position(final int[] object_position) {
    this.object_position = object_position;
  }

  public void setRelation_annotation(final String relation_annotation) {
    this.relation_annotation = relation_annotation;
  }

  public void setSubject(final String subject) {
    this.subject = subject;
  }

  public void setSubject_annotation(final String subject_annotation) {
    this.subject_annotation = subject_annotation;
  }

  public void setSubject_position(final int[] subject_position) {
    this.subject_position = subject_position;
  }
}
