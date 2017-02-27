package uk.ac.shef.dcs.kbproxy;

/**
 *
 */
public class KBProxyException extends Exception {

  /**
  *
  */
  private static final long serialVersionUID = -891737085752216612L;

  public KBProxyException(final Exception e) {
    super(e);
  }

  public KBProxyException(final String msg) {
    super(msg);
  }

  public KBProxyException(final String msg, final Exception e) {
    super(msg, e);
  }
}
