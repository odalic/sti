package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * ExtraRelaTable domain class adapted for REST API (and later mapped to JSON).
 * 
 * @author Václav Brodec
 *
 */
@Immutable
@XmlRootElement(name = "annotation")
public final class AnnotationValue implements Serializable {
	
	private static final long serialVersionUID = -8971845897146434865L;
	
	private List<PropertyValue> properties;
	private List<LabelValue> labels;
	private List<Set<AttributeValuePairValue>> attributeValuePairs;

	private List<StatisticsValue> propertiesStatistics;
	private List<StatisticsValue> labelsStatistics;
	private List<StatisticsValue> pairsStatistics;

	public AnnotationValue() {
		this.properties = ImmutableList.of();
		this.labels = ImmutableList.of();
		this.attributeValuePairs = ImmutableList.of();
		
		this.propertiesStatistics = ImmutableList.of();
		this.labelsStatistics = ImmutableList.of();
		this.pairsStatistics = ImmutableList.of();
	}

	@XmlElement
	public List<PropertyValue> getProperties() {
		return properties;
	}

	public void setProperties(List<? extends PropertyValue> properties) {
		this.properties = ImmutableList.copyOf(properties);
	}

	@XmlElement
	public List<LabelValue> getLabels() {
		return labels;
	}

	public void setLabels(List<? extends LabelValue> labels) {
		this.labels = ImmutableList.copyOf(labels);
	}

	@XmlElement
	public List<Set<AttributeValuePairValue>> getAttributeValuePairs() {
		return attributeValuePairs;
	}

	public void setAttributeValuePairs(List<? extends Set<? extends AttributeValuePairValue>> attributeValuePairs) {
		this.attributeValuePairs = attributeValuePairs.stream().map(set -> ImmutableSet.copyOf(set)).collect(ImmutableList.toImmutableList());
	}

	@XmlElement
	public List<StatisticsValue> getPropertiesStatistics() {
		return propertiesStatistics;
	}

	public void setPropertiesStatistics(List<? extends StatisticsValue> propertiesStatistics) {
		this.propertiesStatistics = ImmutableList.copyOf(propertiesStatistics);
	}

	@XmlElement
	public List<StatisticsValue> getLabelsStatistics() {
		return labelsStatistics;
	}

	public void setLabelsStatistics(List<? extends StatisticsValue> labelsStatistics) {
		this.labelsStatistics = ImmutableList.copyOf(labelsStatistics);
	}

	@XmlElement
	public List<StatisticsValue> getPairsStatistics() {
		return pairsStatistics;
	}

	public void setPairsStatistics(List<? extends StatisticsValue> pairsStatistics) {
		this.pairsStatistics = ImmutableList.copyOf(pairsStatistics);
	}

	@Override
	public String toString() {
		return "AnnotationValue [properties=" + properties + ", labels=" + labels + ", attributeValuePairs="
				+ attributeValuePairs + ", propertiesStatistics=" + propertiesStatistics + ", labelsStatistics="
				+ labelsStatistics + ", pairsStatistics=" + pairsStatistics + "]";
	}
}
