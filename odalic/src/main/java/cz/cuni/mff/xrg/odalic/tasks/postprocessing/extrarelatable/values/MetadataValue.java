package cz.cuni.mff.xrg.odalic.tasks.postprocessing.extrarelatable.values;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "metadata")
public final class MetadataValue implements Serializable {

	private static final long serialVersionUID = -1586827772971166587L;

	private String title;
	private String author;
	private String languageTag;
	private Map<Integer, DeclaredEntityValue> declaredProperties;
	private Map<Integer, DeclaredEntityValue> declaredClasses;
	private Map<Integer, DeclaredEntityValue> collectedProperties;
    private Map<Integer, DeclaredEntityValue> collectedClasses;

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
	public Map<Integer, DeclaredEntityValue> getDeclaredProperties() {
		return Collections.unmodifiableMap(declaredProperties);
	}

	public void setDeclaredProperties(Map<? extends Integer, ? extends DeclaredEntityValue>  declaredProperties) {
		checkNotNull(declaredProperties);
		
		this.declaredProperties = new HashMap<>(declaredProperties);
	}
	
	@XmlElement
	public Map<Integer, DeclaredEntityValue> getDeclaredClasses() {
		return Collections.unmodifiableMap(declaredClasses);
	}

	public void setDeclaredClasses(Map<? extends Integer, ? extends DeclaredEntityValue>  declaredClasses) {
		checkNotNull(declaredClasses);
		
		this.declaredClasses = new HashMap<>(declaredClasses);
	}
	
	@XmlElement
    public Map<Integer, DeclaredEntityValue> getCollectedProperties() {
        return Collections.unmodifiableMap(collectedProperties);
    }

    public void setCollectedProperties(Map<? extends Integer, ? extends DeclaredEntityValue>  collectedProperties) {
        checkNotNull(collectedProperties);
        
        this.collectedProperties = new HashMap<>(collectedProperties);
    }
    
    @XmlElement
    public Map<Integer, DeclaredEntityValue> getCollectedClasses() {
        return Collections.unmodifiableMap(collectedClasses);
    }

    public void setCollectedClasses(Map<? extends Integer, ? extends DeclaredEntityValue>  collectedClasses) {
        checkNotNull(collectedClasses);
        
        this.collectedClasses = new HashMap<>(collectedClasses);
    }

    @Override
    public String toString() {
      return "MetadataValue [title=" + title + ", author=" + author + ", languageTag=" + languageTag
          + ", declaredProperties=" + declaredProperties + ", declaredClasses="
          + declaredClasses + ", collectedProperties=" + collectedProperties
          + ", collectedClasses=" + collectedClasses + "]";
    }
}
