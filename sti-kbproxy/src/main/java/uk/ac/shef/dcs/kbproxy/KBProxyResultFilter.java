package uk.ac.shef.dcs.kbproxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Clazz;

/**
 * Information retrieved from a KB may contain those that are too generic or high level to be
 * useful. E.g., in DBpedia, class 'Thing' may never be a sensible class to annotate a table column.
 *
 * KBProxyResultFilter is responsible for filtering such information. This can be class, relation,
 * or entity, depending on the actual implementing classes.
 */
public class KBProxyResultFilter {
  
  private final Set<String> stoppedClasses;
  private final Set<String> stoppedAttributes;

  public KBProxyResultFilter(final Set<? extends String> stoppedClasses, final Set<? extends String> stoppedAttributes) throws IOException {
    Preconditions.checkNotNull(stoppedClasses);
    Preconditions.checkNotNull(stoppedAttributes);
    
    this.stoppedClasses = ImmutableSet.copyOf(stoppedClasses);
    this.stoppedAttributes = ImmutableSet.copyOf(stoppedAttributes);
  }
  

  /**
   * Creates new list of attributes, which contains only attributes which are valid.
   *
   * @param facts
   * @return
   */
  public List<Attribute> filterAttribute(final Collection<Attribute> facts) {
    final List<Attribute> filteredList = new ArrayList<>();
    for (final Attribute t : facts) {
      if (isValidAttribute(t)) {
        filteredList.add(t);
      }
    }
    return filteredList;
  }

  public List<Clazz> filterClazz(final Collection<Clazz> types) {
    final List<Clazz> r = new ArrayList<>();
    for (final Clazz t : types) {
      if (isValidClazz(t)) {
        r.add(t);
      }
    }
    return r;
  }

  /**
   * Checks whether the attribute is valid attribute. Attribute is valid if it is not blacklisted.
   *
   * @param attribute
   * @return true if the attribute is valid
   */
  protected boolean isValidAttribute(final Attribute attribute) {
    final Set<String> stop = this.stoppedAttributes;
    final String relation = attribute.getRelationURI();
    if (stop != null) {
      for (final String s : stop) {
        if (relation.startsWith(s)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Checks whether the class is valid class. Class is valid if it is not blacklisted.
   *
   * @param c
   * @return true if the class is valid
   */
  protected boolean isValidClazz(final Clazz c) {
    final Set<String> stop = this.stoppedClasses;
    if (stop == null) {
      return true;
    }

    for (final String s : stop) {
      if (c.getId().contains(s) || ((c.getLabel() != null) && c.getLabel().equalsIgnoreCase(s))) {
        return false;
      }
    }

    return true;
  }
}
