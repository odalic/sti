package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 */
public class RelationColumns implements Serializable {
  private static final long serialVersionUID = -7136525814010415943L;

  private int subjectCol;
  private int objectCol;

  public RelationColumns(final int subjectCol, final int objectCol) {
    this.subjectCol = subjectCol;
    this.objectCol = objectCol;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof RelationColumns) {
      final RelationColumns k = (RelationColumns) o;
      return (k.getSubjectCol() == getSubjectCol()) && (k.getObjectCol() == getObjectCol());
    }
    return false;
  }

  public int getObjectCol() {
    return this.objectCol;
  }

  public int getSubjectCol() {
    return this.subjectCol;
  }

  @Override
  public int hashCode() {
    return (this.subjectCol * 19) + (this.objectCol * 29);
  }

  public void setObjectCol(final int objectCol) {
    this.objectCol = objectCol;
  }

  public void setSubjectCol(final int subjectCol) {
    this.subjectCol = subjectCol;
  }

  @Override
  public String toString() {
    return getSubjectCol() + "-" + getObjectCol();
  }
}
