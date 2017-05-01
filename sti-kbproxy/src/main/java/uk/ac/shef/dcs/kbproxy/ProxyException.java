package uk.ac.shef.dcs.kbproxy;

public class ProxyException extends Exception {

  private static final long serialVersionUID = -891737085752216612L;

  public ProxyException(final Exception e) {
    super(e);
  }

  public ProxyException(final String msg) {
    super(msg);
  }

  public ProxyException(final String msg, final Exception e) {
    super(msg, e);
  }
}
