package cz.cuni.mff.xrg.odalic.entities;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import cz.cuni.mff.xrg.odalic.api.rest.adapters.PropertyTypeAdapter;

@XmlJavaTypeAdapter(PropertyTypeAdapter.class)
public enum PropertyType {
  DATA, OBJECT;
}
