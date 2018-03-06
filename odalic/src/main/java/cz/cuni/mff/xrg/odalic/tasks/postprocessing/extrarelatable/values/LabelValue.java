package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableList;

@XmlRootElement(name = "label")
public final class LabelValue implements Serializable {
	
	private static final long serialVersionUID = 9122163600617727467L;
	
	private String text;
	private String description;
	private boolean synthetic;
	
	private int index;
	private String file;
	private List<String> firstValues;
	private List<String> headers;
	private List<List<String>> firstRows; 
	
	public LabelValue() {
		this.text = null;
		this.description = null;
		this.synthetic = false;
		this.index = Integer.MIN_VALUE;
		this.file = null;
		this.firstValues = ImmutableList.of();
		this.headers = ImmutableList.of();
		this.firstRows = ImmutableList.of();
	}

	@XmlElement
	@Nullable
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@XmlElement
	@Nullable
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement
	public boolean isSynthetic() {
		return synthetic;
	}

	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}

	@XmlElement
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		checkArgument(index >= 0, "The index must be non-negative!");
		
		this.index = index;
	}

	@XmlElement
	@Nullable
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		checkNotNull(file);
		
		this.file = file;
	}

	@XmlElement
	public List<String> getFirstValues() {
		return firstValues;
	}

	public void setFirstValues(List<String> firstValues) {
		this.firstValues = ImmutableList.copyOf(firstValues);
	}

	@XmlElement
	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = ImmutableList.copyOf(headers);
	}

	@XmlElement
	public List<List<String>> getFirstRows() {
		return firstRows;
	}

	public void setFirstRows(List<List<String>> firstRows) {
		this.firstRows = firstRows.stream().map(row -> ImmutableList.copyOf(row)).collect(ImmutableList.toImmutableList());
	}

	@Override
	public String toString() {
		return "LabelValue [text=" + text + ", description=" + description + ", synthetic=" + synthetic + ", index="
				+ index + ", file=" + file + ", firstValues=" + firstValues + ", headers=" + headers + ", firstRows="
				+ firstRows + "]";
	}
}
