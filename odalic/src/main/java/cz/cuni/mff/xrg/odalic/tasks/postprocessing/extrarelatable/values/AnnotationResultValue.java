package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.collect.ImmutableMap;

@XmlRootElement(name = "annotationResult")
public class AnnotationResultValue {
	
	private Map<Integer, AnnotationValue> annotations;

	public AnnotationResultValue() {
		this.annotations = ImmutableMap.of();
	}
	
	@XmlElement
	public Map<Integer, AnnotationValue> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(final Map<? extends Integer, ? extends AnnotationValue> annotations) {
		this.annotations = ImmutableMap.copyOf(annotations);
	}

	@Override
	public String toString() {
		return "AnnotationResultValue [annotations=" + annotations + "]";
	}
}
