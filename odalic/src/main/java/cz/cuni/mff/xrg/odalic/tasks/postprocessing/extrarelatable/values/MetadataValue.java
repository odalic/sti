package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.ImmutableMap;

@XmlRootElement(name = "metadata")
public final class MetadataValue implements Serializable {

	private static final long serialVersionUID = -1586827772971166587L;

	private String title;
	private String author;
	private String languageTag;
	private Map<Integer, URI> declaredPropertyUris;
	private Map<Integer, URI> declaredClassUris;
	private Map<Integer, URI> collectedPropertyUris;
    private Map<Integer, URI> collectedClassUris;

	@XmlElement
	@Nullable
	public String getTitle() {
		return title;
	}

	public void setTitle(@Nullable String title) {
		this.title = title;
	}

	@XmlElement
	@Nullable
	public String getAuthor() {
		return author;
	}

	public void setAuthor(@Nullable String author) {
		this.author = author;
	}

	@XmlElement
	@Nullable
	public String getLanguageTag() {
		return languageTag;
	}

	public void setLanguageTag(@Nullable String languageTag) {
		this.languageTag = languageTag;
	}
	
	@XmlElement
	public Map<Integer, URI> getDeclaredPropertyUris() {
		return declaredPropertyUris;
	}

	public void setDeclaredPropertyUris(Map<? extends Integer, ? extends URI>  declaredPropertyUris) {
		checkNotNull(declaredPropertyUris);
		
		this.declaredPropertyUris = ImmutableMap.copyOf(declaredPropertyUris);
	}
	
	@XmlElement
	public Map<Integer, URI> getDeclaredClassUris() {
		return declaredClassUris;
	}

	public void setDeclaredClassUris(Map<? extends Integer, ? extends URI>  declaredClassUris) {
		checkNotNull(declaredClassUris);
		
		this.declaredClassUris = ImmutableMap.copyOf(declaredClassUris);
	}
	
	@XmlElement
    public Map<Integer, URI> getCollectedPropertyUris() {
        return collectedPropertyUris;
    }

    public void setCollectedPropertyUris(Map<? extends Integer, ? extends URI>  collectedPropertyUris) {
        checkNotNull(collectedPropertyUris);
        
        this.collectedPropertyUris = ImmutableMap.copyOf(collectedPropertyUris);
    }
    
    @XmlElement
    public Map<Integer, URI> getCollectedClassUris() {
        return collectedClassUris;
    }

    public void setCollectedClassUris(Map<? extends Integer, ? extends URI>  collectedClassUris) {
        checkNotNull(collectedClassUris);
        
        this.collectedClassUris = ImmutableMap.copyOf(collectedClassUris);
    }

    @Override
    public String toString() {
      return "MetadataValue [title=" + title + ", author=" + author + ", languageTag=" + languageTag
          + ", declaredPropertyUris=" + declaredPropertyUris + ", declaredClassUris="
          + declaredClassUris + ", collectedPropertyUris=" + collectedPropertyUris
          + ", collectedClassUris=" + collectedClassUris + "]";
    }
}
