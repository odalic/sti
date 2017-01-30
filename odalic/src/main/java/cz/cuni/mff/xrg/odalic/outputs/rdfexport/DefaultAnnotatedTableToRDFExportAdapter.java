package cz.cuni.mff.xrg.odalic.outputs.rdfexport;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable;
import cz.cuni.mff.xrg.odalic.outputs.annotatedtable.TableColumn;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp.DataPropertyTriplePattern;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp.ObjectListPropertyTriplePattern;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp.ObjectPropertyTriplePattern;
import cz.cuni.mff.xrg.odalic.outputs.rdfexport.tp.TriplePattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default {@link AnnotatedTableToRDFExportAdapter} implementation.
 * 
 * @author Josef Janoušek
 * @author Tomáš Knap
 *
 */
public class DefaultAnnotatedTableToRDFExportAdapter implements AnnotatedTableToRDFExportAdapter {

  private static final Logger log = LoggerFactory.getLogger(DefaultAnnotatedTableToRDFExportAdapter.class);
  
  private static final String PREFIX_SEPARATOR = ":";
  
  private ValueFactory factory = SimpleValueFactory.getInstance();
  
  private Map<String, String> prefixes;
  
  /**
   * The default toRDFExport implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.rdfexport.AnnotatedTableToRDFExportAdapter#toRDFExport(cz.cuni.mff.xrg.odalic.outputs.annotatedtable.AnnotatedTable, cz.cuni.mff.xrg.odalic.input.Input)
   */
  @Override
  public Model toRDFExport(AnnotatedTable annotatedTable, Input extendedInput) {

    // map for prefixes
    prefixes = annotatedTable.getContext().getMapping();
    
    // map for accessing column positions by column names in input
    Map<String, Integer> positionsForColumnNames = new HashMap<>();
    int i = 0;
    for (String headerName : extendedInput.headers()) {
      positionsForColumnNames.put(headerName, i);
      i++;
    }

    // fetch the triplePatterns from annotated table
    log.debug("Preparing set of triple patterns to be applied to all rows");
    List<TriplePattern> triplePatterns = new ArrayList<>();
    List<Statement> tripleStatements = new ArrayList<>();
    for (TableColumn column : annotatedTable.getTableSchema().getColumns()) {
      if (column.getSuppressOutput() != null && column.getSuppressOutput()) {
        // we do not create any triple for the suppressed column
        log.debug("Column has suppressed output, we do not create any triple for {}", column.getName());
        continue;
      }
      if (StringUtils.isEmpty(column.getPropertyUrl())) {
        log.warn("PropertyUrl is not defined for the column {}, no triple is created for that column", column.getName());
        continue;
      }
      if (StringUtils.isEmpty(column.getAboutUrl())) {
        log.warn("AboutUrl is not defined for the column {}, no triple is created for that column", column.getName());
        // Currently we require aboutUrl to be defined for all columns. Nevertheless, based on the spec, this is not required and
        //  if aboutUrl is not defined on the column, it may be e.g. defined at the level of whole tableScheme.
        // Also aboutUrl may contain more complex patterns, e.g.: "aboutUrl": "http://example.org/tree/{on_street}/{GID}", but so far we expect that
        //  it contains only "{columnName}" or complete IRI
        continue;
      }

      if (!isColumnLink(column.getAboutUrl())) {
        // aboutUrl is not a column link, so we create just one triple statement, not a triple pattern
        try {
          tripleStatements.add(factory.createStatement(createIRI(column.getAboutUrl()),
              createIRI(column.getPropertyUrl()), createIRIorLiteral(column.getValueUrl())));
        } catch (NullPointerException | IllegalArgumentException e) {
          log.error("No triple statement is produced because of " + e);
        }
        continue;
      }

      TriplePattern tp;
      if (column.getValueUrl() == null) {
        // if valueUrl is null, than we now that we should generate triple with data property (object is literal)
        tp = new DataPropertyTriplePattern(column.getAboutUrl(), createIRI(column.getPropertyUrl()), column.getName());

      }
      else {
        // it is object property
        // so far we suppose that valueUrl contains either the URL itself or pattern in the form {columnName}, meaning that URL is taken from that column.
        if (StringUtils.isEmpty(column.getSeparator())) {
          tp = new ObjectPropertyTriplePattern(column.getAboutUrl(), createIRI(column.getPropertyUrl()), column.getValueUrl());
        }
        else {
          // if separator is not empty, valueUrl contains list of values
          tp = new ObjectListPropertyTriplePattern(column.getAboutUrl(), createIRI(column.getPropertyUrl()), column.getValueUrl(), column.getSeparator());
        }
      }
      triplePatterns.add(tp);

    }
    
    // create a new Model to put statements in
    Model model = new LinkedHashModel();
    
    // set prefixes
    for (Map.Entry<String, String> prefixEntry : prefixes.entrySet()) {
      model.setNamespace(prefixEntry.getKey(), prefixEntry.getValue());
    }
    
    // add created triple statements
    model.addAll(tripleStatements);
    
    // process the rows from extended input
    log.debug("Iterating over set of row and creating triples for each row");
    IRI subject;
    List<Value> objects = new ArrayList<>();
    for (List<String> row : extendedInput.rows()) {
      for (TriplePattern tp : triplePatterns) {
        
        // create the subject
        // currently we expect only subject patterns of the form {columnName}
        String columnSubjectName = getColumnName(tp.getSubjectPattern());
        if (positionsForColumnNames.get(columnSubjectName) == null) {
          // column with that name does not exist, so we can not produce the triple
          log.warn("Column named '{}' does not exist, no triple is produced", columnSubjectName);
          continue;
        }
        int subjectPosition = positionsForColumnNames.get(columnSubjectName);
        if (StringUtils.isEmpty(row.get(subjectPosition))) {
          // value in column with that name does not exist (in current row), so we can not produce the triple
          log.warn("Value in column named '{}' does not exist, no triple is produced", columnSubjectName);
          continue;
        }
        try {
          subject = createIRI(row.get(subjectPosition));
        } catch (NullPointerException | IllegalArgumentException e) {
          log.error("No triple is produced because of " + e);
          continue;
        }
        
        // create the object(s)
        objects.clear();
        if (tp instanceof DataPropertyTriplePattern) {
          // it is data property
          String columnName = ((DataPropertyTriplePattern)tp).getObjectColumnName();
          if (positionsForColumnNames.get(columnName) == null) {
            // column with that name does not exist, so we can not produce the triple
            log.warn("Column named '{}' does not exist, no triple is produced", columnName);
            continue;
          }
          int objectPosition = positionsForColumnNames.get(columnName);
          if (StringUtils.isEmpty(row.get(objectPosition))) {
            // value in column with that name does not exist (in current row), so we can not produce the triple
            log.warn("Value in column named '{}' does not exist, no triple is produced", columnName);
            continue;
          }
          objects.add(factory.createLiteral(row.get(objectPosition)));
        }
        else if (tp instanceof ObjectListPropertyTriplePattern) {
          // it is object property containing list of values
          ObjectListPropertyTriplePattern oltp = (ObjectListPropertyTriplePattern) tp;
          if (isColumnLink(oltp.getObjectPattern())) {
            String columnName = getColumnName(oltp.getObjectPattern());
            if (positionsForColumnNames.get(columnName) == null) {
              // column with that name does not exist, so we can not produce the triple
              log.warn("Column named '{}' does not exist, no triple is produced", columnName);
              continue;
            }
            int objectPosition = positionsForColumnNames.get(columnName);
            if (StringUtils.isEmpty(row.get(objectPosition))) {
              // value in column with that name does not exist (in current row), so we can not produce the triple
              log.warn("Value in column named '{}' does not exist, no triple is produced", columnName);
              continue;
            }
            for (String item : row.get(objectPosition).split(oltp.getSeparator())) {
              try {
                objects.add(createIRI(item));
              } catch (NullPointerException | IllegalArgumentException e) {
                log.error("No triple is produced because of " + e);
              }
            }
          }
          else {
            // object pattern contains URIs:
            for (String item : oltp.getObjectPattern().split(oltp.getSeparator())) {
              try {
                objects.add(createIRI(item));
              } catch (NullPointerException | IllegalArgumentException e) {
                log.error("No triple is produced because of " + e);
              }
            }
          }
        }
        else if (tp instanceof ObjectPropertyTriplePattern) {
          // it is object property
          ObjectPropertyTriplePattern otp = (ObjectPropertyTriplePattern) tp;
          if (isColumnLink(otp.getObjectPattern())) {
            String columnName = getColumnName(otp.getObjectPattern());
            if (positionsForColumnNames.get(columnName) == null) {
              // column with that name does not exist, so we can not produce the triple
              log.warn("Column named '{}' does not exist, no triple is produced", columnName);
              continue;
            }
            int objectPosition = positionsForColumnNames.get(columnName);
            if (StringUtils.isEmpty(row.get(objectPosition))) {
              // value in column with that name does not exist (in current row), so we can not produce the triple
              log.warn("Value in column named '{}' does not exist, no triple is produced", columnName);
              continue;
            }
            try {
              objects.add(createIRIorLiteral(row.get(objectPosition)));
            } catch (NullPointerException | IllegalArgumentException e) {
              log.error("No triple is produced because of " + e);
            }
          }
          else {
            // object pattern contains URI:
            try {
              objects.add(createIRI(otp.getObjectPattern()));
            } catch (NullPointerException | IllegalArgumentException e) {
              log.error("No triple is produced because of " + e);
            }
          }
        }
        else {
          log.error("Unsupported Triple Pattern");
        }
        
        // add the RDF statement by providing subject, predicate and object
        for (Value object : objects) {
          model.add(subject, tp.getPredicate(), object);
        }
      }
    }
    
    return model;
  }
  
  /**
   * returns true when the value starts with "{" and ends with "}"
   * 
   * @param value
   * @return
   */
  private boolean isColumnLink(String value) {
    return value.startsWith("{") && value.endsWith("}");
  }
  
  /**
   * Takes the column link value from Annotated table (e.g. "{Book_url}")
   * and returns only linked column name without brackets (e.g. "Book_url").
   * 
   * @param columnLink column link with brackets
   * @return column name without brackets
   */
  private String getColumnName(String columnLink) {
    return columnLink.substring(1, columnLink.length()-1);
  }
  
  /**
   * Checks validity of IRI from the supplied string-representation.
   * 
   * @param iriString
   *        A string-representation of the IRI.
   * @return true If the supplied string does not resolve to a legal IRI
   *         (i.e. if it does not contain a colon).
   */
  private boolean notValidIRI(String iriString) {
    return iriString.indexOf(PREFIX_SEPARATOR) < 0;
  }
  
  /**
   * Creates a new IRI from the supplied string-representation.
   * 
   * @param iriString
   *        A string-representation of the IRI.
   * @return An object representing the IRI.
   * @throws IlllegalArgumentException
   *         If the supplied string does not resolve to a legal IRI
   *         (i.e. if it does not contain a colon).
   */
  private IRI createIRI(String iriString) {
    if (notValidIRI(iriString)) {
      throw new IllegalArgumentException("Not a valid (absolute) IRI: " + iriString);
    }
    
    String[] array = iriString.split(PREFIX_SEPARATOR);
    if (array.length == 2 && !array[1].startsWith("//") &&
        prefixes.containsKey(array[0])) {
      // IRI expansion
      return factory.createIRI(prefixes.get(array[0]), array[1]);
    }
    else {
      return factory.createIRI(iriString);
    }
  }
  
  private Value createIRIorLiteral(String object) {
    if (notValidIRI(object)) {
      log.info("Not a valid (absolute) IRI: " + object + " , literal will be created.");
      return factory.createLiteral(object);
    }
    else {
      return createIRI(object);
    }
  }
}
