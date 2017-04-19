package cz.cuni.mff.xrg.odalic.bases;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.TextSearchingMethodAdapter;

@XmlJavaTypeAdapter(TextSearchingMethodAdapter.class)
public enum TextSearchingMethod {
  EXACT,

  FULLTEXT,

  SUBSTRING
}
