package cz.cuni.mff.xrg.odalic.input;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.formats.Format;

/**
 * <p>
 * This class represents result of CSV file parsing by {@link CsvInputParser}.
 * The parse method returns {@link Input} which creates from the file
 * and {@link Format} which creates by setting of detected line separator
 * used in the file to provided {@link Format}.
 * </p>
 * 
 * @author Josef Janoušek
 *
 */
@Immutable
public class ParsingResult implements Serializable {
  
  private static final long serialVersionUID = 164936506495425123L;
  
  private final Input input;
  
  private final Format format;
  
  /**
   * Creates new ParsingResult.
   * 
   * @param input input parsed from CSV file
   * @param format format with detected line separator
   */
  public ParsingResult(Input input, Format format) {
    Preconditions.checkNotNull(input);
    Preconditions.checkNotNull(format);
    
    this.input = input;
    this.format = format;
  }
  
  /**
   * @return the input
   */
  public Input getInput() {
    return input;
  }
  
  /**
   * @return the format
   */
  public Format getFormat() {
    return format;
  }
  
  /**
   * Computes hash code based on all its parts.
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((input == null) ? 0 : input.hashCode());
    result = prime * result + ((format == null) ? 0 : format.hashCode());
    return result;
  }
  
  /**
   * Compares to another object for equality (only another ParsingResult composed from equal parts passes).
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ParsingResult other = (ParsingResult) obj;
    if (input == null) {
      if (other.input != null) {
        return false;
      }
    } else if (!input.equals(other.input)) {
      return false;
    }
    if (format == null) {
      if (other.format != null) {
        return false;
      }
    } else if (!format.equals(other.format)) {
      return false;
    }
    return true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ParsingResult [input=" + input + ", format=" + format + "]";
  }
}
