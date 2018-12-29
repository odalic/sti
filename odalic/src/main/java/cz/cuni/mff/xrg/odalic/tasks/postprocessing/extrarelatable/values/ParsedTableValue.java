package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.google.common.collect.ImmutableList;

/**
 * ExtraRelaTable domain class adapted for REST API (and later mapped to JSON).
 * 
 * @author Václav Brodec
 *
 */
@XmlRootElement(name = "parsedTable")
@XmlAccessorType(XmlAccessType.NONE)
@Immutable
public final class ParsedTableValue implements Serializable {

	private static final long serialVersionUID = 4101912998363935336L;

	private List<List<String>> rows;

	private List<String> headers;

	private MetadataValue metadata;
	
	public ParsedTableValue() {
		this.rows = ImmutableList.of();
		this.headers = ImmutableList.of();
		this.metadata = null;
	}

	
	@XmlElement
	public List<List<String>> getRows() {
		return rows;
	}



	public void setRows(List<List<String>> rows) {
		checkNotNull(rows);
		
		this.rows = rows.stream().map(row -> ImmutableList.copyOf(row)).collect(ImmutableList.toImmutableList());
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
	public MetadataValue getMetadata() {
		return metadata;
	}

	public void setMetadata(MetadataValue metadata) {
		this.metadata = metadata;
	}



	@Override
	public String toString() {
		return "ParsedTableValue [rows=" + rows + ", headers=" + headers + ", metadata=" + metadata + "]";
	}
}
