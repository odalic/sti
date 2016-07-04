package cz.cuni.mff.xrg.odalic.api.rest.values;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.tasks.annotations.Likelihood;

@XmlRootElement(name = "likelihood")
public final class LikelihoodValue implements Serializable {
  
  private static final long serialVersionUID = -901650058091668104L;
  
  @XmlElement
  private double value;

  public LikelihoodValue() {}
  
  public LikelihoodValue(Likelihood adaptee) {
    value = adaptee.getValue();
  }

  /**
   * @return the value
   */
  public double getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(double value) {
    Preconditions.checkArgument(value >= 0);
    Preconditions.checkArgument(value <= 1);
    
    this.value = value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "LikelihoodValue [value=" + value + "]";
  }
}