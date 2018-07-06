package cz.cuni.mff.xrg.odalic.api.rest.values;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.collect.ImmutableList;

/**
 * @author Václav Brodec
 */
@XmlRootElement(name = "computationInput")
@XmlAccessorType(XmlAccessType.NONE)
@Immutable
public final class ComputationInputValue implements Serializable {

  private static final long serialVersionUID = 4101912998363935336L;

  private String[][] rows;

  private List<String> headers;

  private String identifier;

  private ComputationInputValue() {
    this.rows = new String[0][0];
    this.headers = ImmutableList.of();
    this.identifier = null;
  }

  @XmlElement
  public String[][] getRows() {
    return cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(String.class, this.rows);
  }

  public void setRows(final String[][] rows) {
    checkNotNull(rows, "The rows cannot be null!");

    this.rows = cz.cuni.mff.xrg.odalic.util.Arrays.deepCopy(String.class, rows);
  }

  @XmlElement
  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(List<String> headers) {
    checkNotNull(headers, "The headers cannot be null!");

    this.headers = ImmutableList.copyOf(headers);
  }

  @XmlElement
  @Nullable
  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    checkNotNull(identifier, "The identifier cannot be null!");

    this.identifier = identifier;
  }

  @Override
  public String toString() {
    return "ComputationInputValue [rows=" + Arrays.toString(rows) + ", headers=" + headers
        + ", identifier=" + identifier + "]";
  }
}
