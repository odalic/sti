package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ExtraRelaTable domain class adapted for REST API (and later mapped to JSON).
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "attributeValuePair")
public final class AttributeValuePairValue implements Serializable {
	
	private static final long serialVersionUID = 6583830370968803352L;
	
	private String attribute;
	private String value;
	
	@XmlElement
	@Nullable
	public String getAttribute() {
		return attribute;
	}

	@XmlElement
	@Nullable
	public String getValue() {
		return value;
	}
	
	public void setAttribute(@Nullable String attribute) {
		this.attribute = attribute;
	}

	public void setValue(@Nullable String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "AttributeValuePairValue [attribute=" + attribute + ", value=" + value + "]";
	}
}
