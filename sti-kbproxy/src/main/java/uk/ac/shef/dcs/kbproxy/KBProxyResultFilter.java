package uk.ac.shef.dcs.kbproxy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Clazz;

/**
 * Information retrieved from a KB may contain those that are too generic or high level to be
 * useful. E.g., in DBpedia, class 'Thing' may never be a sensible class to annotate a table column.
 *
 * KBProxyResultFilter is responsible for filtering such information. This can be class, relation,
 * or entity, depending on the actual implementing classes.
 */
public abstract class KBProxyResultFilter {
  protected static final String LABEL_INVALID_CLAZZ = "!invalid_clazz";
  protected static final String LABEL_INVALID_ATTRIBUTE = "!invalid_attribute";
  protected Map<String, Set<String>> stopLists = new HashMap<>();

  public KBProxyResultFilter(final String stopListsFile) throws IOException {
    loadStopLists(stopListsFile);
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

    final Set<String> stop = this.stopLists.get(LABEL_INVALID_ATTRIBUTE);
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

    final Set<String> stop = this.stopLists.get(LABEL_INVALID_CLAZZ);
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


  /**
   * An external file defining class/relation/entities to be filtered. the file must correspond to
   * certain format. See 'resources/kbstoplist.txt' for explanation
   *
   * It loads all given classes/relations/entities to a Map, which contains as the key for the set
   * of stop wrods the label obtained from the file (from the line starting with !). So there are
   * different stop words for attributes and classes, e.g.
   *
   * @param stopListsFile
   * @throws IOException
   */
  protected void loadStopLists(final String stopListsFile) throws IOException {
    final LineIterator it = FileUtils.lineIterator(new File(stopListsFile));
    String label = "";
    Set<String> elements = new HashSet<>();
    while (it.hasNext()) {
      final String line = it.nextLine().trim();

      if ((line.length() < 1) || line.startsWith("#")) {
        continue;
      }

      if (line.startsWith("!")) {
        if (elements.size() > 0) {
          this.stopLists.put(label, elements);
        }

        elements = new HashSet<>();
        label = line;
      } else {
        elements.add(line);
      }
    }
    if (elements.size() != 0) {
      this.stopLists.put(label, elements);
    }
  }

}
