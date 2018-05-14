package cz.cuni.mff.xrg.odalic.api.rest.values;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * @author Václav Brodec
 */
@XmlRootElement(name = "computationInput")
@XmlAccessorType(XmlAccessType.NONE)
@Immutable
public final class ComputationInputValue implements Serializable {

  private static final long serialVersionUID = 4101912998363935336L;

  private List<List<String>> rows;

  private List<String> headers;

  private String identifier;

  private ComputationInputValue() {
    this.rows = ImmutableList.of();
    this.headers = ImmutableList.of();
    this.identifier = null;
  }

  @XmlElement
  public List<List<String>> getRows() {
    return rows;
  }

  public void setRows(List<List<String>> rows) {
    checkNotNull(rows);

    this.rows = rows.stream().map(row -> ImmutableList.copyOf(row))
        .collect(ImmutableList.toImmutableList());
  }
  
  @XmlElement
  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(List<String> headers) {
    checkNotNull(headers);

    this.headers = ImmutableList.copyOf(headers);
  }

  @XmlElement
  @Nullable
  public String getIdentifier() { 
    return identifier;
  }

  public void setIdentifier(String identifier) {
    Preconditions.checkNotNull(identifier);
    
    this.identifier = identifier;
  }

  @Override
  public String toString() {
    return "ComputationInputValue [rows=" + rows + ", headers=" + headers + ", identifier=" + identifier
        + "]";
  }
}
