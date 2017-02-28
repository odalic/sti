package uk.ac.shef.dcs.sti;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 22/10/12 Time: 12:42
 */
public class STIException extends Exception {
  /**
  *
  */
  private static final long serialVersionUID = 1001679235746774903L;

  public STIException(final Exception e) {
    super(e);
  }

  public STIException(final String s) {
    super(s);
  }

  public STIException(final String s, final Exception e) {
    super(s, e);
  }

}
