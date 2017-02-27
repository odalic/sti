package uk.ac.shef.dcs.kbproxy.sparql;

import javafx.util.Pair;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.util.Asserts;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.Levenshtein;
import uk.ac.shef.dcs.kbproxy.KBDefinition;
import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.KBProxyUtils;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class SPARQLProxy extends KBProxy {

  private static final String SPARQL_PREFIX = "PREFIX %1$s: <%2$s>";
  private static final String INSERT_BASE = "INSERT DATA {GRAPH <%1$s> {%2$s .}}";

  private static final String SPARQL_VARIABLE_SUBJECT = "?subject";
  private static final String SPARQL_VARIABLE_PREDICATE = "?predicate";
  protected static final String SPARQL_VARIABLE_OBJECT = "?object";
  private static final String SPARQL_VARIABLE_CLASS = "?class";
  private static final String SPARQL_VARIABLE_TEMP_INSTANCE = "?temp_instance";

  private static final String SPARQL_PREDICATE_BIF_CONTAINS = "<bif:contains>";

  private static final String SPARQL_FILTER_REGEX = "regex (str(%1$s), %2$s, \"i\")";

  private static final String SPARQL_STRING_LITERAL = "\"%1$s\"";
  private static final String SPARQL_RESOURCE = "<%1$s>";

  /**
   * Escape patterns from http://www.w3.org/TR/rdf-sparql-query/#grammarEscapes
   */
  private static final Map<Character, String> SPARQL_ESCAPE_REPLACEMENTS;

  static {
    Map<Character, String> map = new HashMap<>();

    map.put('\t', "\\t");
    map.put('\n', "\\n");
    map.put('\r', "\\r");
    map.put('\b', "\\b");
    map.put('\f', "\\f");
    map.put('\"', "\\\"");
    map.put('\'', "\\'");
    map.put('\\', "\\\\");

    SPARQL_ESCAPE_REPLACEMENTS = Collections.unmodifiableMap(map);
  }

  protected StringMetric stringMetric = new Levenshtein();

  /**
   * @param kbDefinition    the definition of the knowledge base.
   * @param fuzzyKeywords   given a query string, kbproxy will firstly try to fetch results matching the exact query. when no match is
   *                        found, you can set fuzzyKeywords to true, to let kbproxy to break the query string based on conjunective words.
   *                        So if the query string is "tom and jerry", it will try "tom" and "jerry"
   * @param cachesBasePath  Base path for the initialized solr caches.
   * @throws IOException
   */
  public SPARQLProxy(KBDefinition kbDefinition,
                     Boolean fuzzyKeywords,
                     String cachesBasePath,
                     Map<String, String> prefixToUriMap) throws IOException, KBProxyException {
    super(kbDefinition, fuzzyKeywords, cachesBasePath, prefixToUriMap);

    if (kbDefinition.getPredicateLabel().size() == 0) {
      throw new KBProxyException("KB definition contains no label predicates.");
    }
  }

  /**
   * Example fulltext query:
   * select distinct ?Subject ?Object where
   * {
   *   {?Subject <http://xmlns.com/foaf/0.1/name> ?Object . ?Object <bif:contains> "Obama"} UNION {?Subject <http://www.w3.org/2000/01/rdf-schema#label> ?Object . ?Object <bif:contains> "Obama"} .
   *   {?Subject a <http://dbpedia.org/ontology/NaturalPlace>} UNION {?Subject a <http://dbpedia.org/ontology/WrittenWork>} .
   *   ?Subject a ?Class .
   *   {?Class a <http://www.w3.org/2002/07/owl#Class>} UNION {?Class a <http://www.w3.org/2000/01/rdf-schema#class>} .
   * } LIMIT 100
   *
   * Example regex query
   * select distinct ?Subject ?Object where
   * {
   *   {?Subject <http://xmlns.com/foaf/0.1/name> ?Object} UNION {?Subject <http://www.w3.org/2000/01/rdf-schema#label> ?Object} .
   *   {?Subject a <http://dbpedia.org/ontology/NaturalPlace>} UNION {?Subject a <http://dbpedia.org/ontology/WrittenWork>} .
   *   ?Subject a ?Class .
   *   {?Class a <http://www.w3.org/2002/07/owl#Class>} UNION {?Class a <http://www.w3.org/2000/01/rdf-schema#class>} .
   *   filter regex (str(?Object), "Obama", "i")
   * } LIMIT 100
   *
   * @param content string to search
   * @param limit maximum number of items to return
   * @param restrictClassTypes true if types of resources should be classes
   * @param types restricts the result types
   * @return SPARQL select query
   * @throws ParseException Invalid regex expression
   * @throws KBProxyException Invalid KB definition
   */
  protected Query createFulltextQueryForResources(String content, Integer limit, boolean restrictClassTypes, String... types) throws ParseException {
    SelectBuilder builder = createFulltextQueryBuilder(content, limit, types);

    // Class restriction
    builder = addClassRestriction(builder, restrictClassTypes);

    return builder.build();
  }

  protected Query createFulltextQueryForClasses(String content, Integer limit) throws ParseException {
    Set<String> classTypes = kbDefinition.getStructureClass();
    SelectBuilder builder = createFulltextQueryBuilder(content, limit, classTypes.toArray(new String[classTypes.size()]));

    if (classTypes.size() == 0) {
      // If there are no class definitions, then add restriction on existence of instances.
      builder = builder.addWhere(SPARQL_VARIABLE_TEMP_INSTANCE, createSPARQLResource(kbDefinition.getStructureInstanceOf()), SPARQL_VARIABLE_SUBJECT);
    }

    return builder.build();
  }

  protected Query createFulltextQueryForPredicates(String content, Integer limit, String domain, String range) throws ParseException {
    Set<String> predicateTypes = kbDefinition.getStructureProperty();
    SelectBuilder builder = createFulltextQueryBuilder(content, limit, predicateTypes.toArray(new String[predicateTypes.size()]));

    if (!isNullOrEmpty(domain)) {
      builder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(kbDefinition.getStructureDomain()), createSPARQLResource(domain));
    }

    if (!isNullOrEmpty(range)) {
      builder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(kbDefinition.getStructureRange()), createSPARQLResource(range));
    }

    return builder.build();
  }

  protected Query createExactMatchQueryForResources(String content, Integer limit, boolean restrictClassTypes, String... types) throws ParseException {
    SelectBuilder builder = createExactMatchQueryBuilder(content, limit, types);

    // Class restriction
    builder = addClassRestriction(builder, restrictClassTypes);

    return builder.build();
  }

  protected Query createExactMatchQueryForClasses(String content, Integer limit) throws ParseException {
    Set<String> classTypes = kbDefinition.getStructureClass();
    SelectBuilder builder = createExactMatchQueryBuilder(content, limit, classTypes.toArray(new String[classTypes.size()]));

    if (classTypes.size() == 0) {
      // If there are no class definitions, then add restriction on existence of instances.
      builder = builder.addWhere(SPARQL_VARIABLE_TEMP_INSTANCE, createSPARQLResource(kbDefinition.getStructureInstanceOf()), SPARQL_VARIABLE_SUBJECT);
    }

    return builder.build();
  }

  protected Query createExactMatchQueryForPredicates(String content, Integer limit, String domain, String range) throws ParseException {
    Set<String> predicateTypes = kbDefinition.getStructureProperty();
    SelectBuilder builder = createExactMatchQueryBuilder(content, limit, predicateTypes.toArray(new String[predicateTypes.size()]));

    if (!isNullOrEmpty(domain)) {
      builder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(kbDefinition.getStructureDomain()), createSPARQLResource(domain));
    }

    if (!isNullOrEmpty(range)) {
      builder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(kbDefinition.getStructureRange()), createSPARQLResource(range));
    }

    return builder.build();
  }

  protected Query createGetLabelQuery(String resourceUrl) {
    resourceUrl = resourceUrl.replaceAll("\\s+", "");

    SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_OBJECT);
    if (kbDefinition.getPredicateLabel().size() == 1) {
      builder = builder.addWhere(createSPARQLResource(resourceUrl), createSPARQLResource(kbDefinition.getPredicateLabel().iterator().next()), SPARQL_VARIABLE_OBJECT);
    }
    else {
      for (String labelPredicate : kbDefinition.getPredicateLabel()) {
        SelectBuilder unionBuilder = new SelectBuilder();
        unionBuilder = unionBuilder.addWhere(createSPARQLResource(resourceUrl), createSPARQLResource(labelPredicate), SPARQL_VARIABLE_OBJECT);

        builder = builder.addUnion(unionBuilder);
      }
    }

    return builder.build();
  }

  protected boolean ask(Query sparqlQuery) {
    log.info("SPARQL query: \n" + sparqlQuery.toString());

    QueryExecution queryExecution = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), sparqlQuery);

    return queryExecution.execAsk();
  }


  /**
   * Returns the entity URL and if available, also label of that entity)
   * @param query
   * @return
   */
  protected List<Pair<String, String>> queryReturnTuples(Query query, String defaultObjectValue) {
    return  queryReturnNodeTuples(query).stream()
            .map(item -> new Pair<>(item.getKey().toString(), item.getValue() != null ? item.getValue().toString() : defaultObjectValue))
            .collect(Collectors.toList());
  }

  protected List<Pair<RDFNode, RDFNode>> queryReturnNodeTuples(Query query) {
    log.info("SPARQL query: \n" + query.toString());

    QueryExecution qExec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

    List<Pair<RDFNode, RDFNode>> out = new ArrayList<>();
    ResultSet rs = qExec.execSelect();
    while (rs.hasNext()) {

      QuerySolution qs = rs.next();
      RDFNode subject = qs.get(SPARQL_VARIABLE_SUBJECT);
      RDFNode object = qs.get(SPARQL_VARIABLE_OBJECT);

      out.add(new Pair<>(subject, object));
    }
    return out;
  }

  /**
   * Returns the entity URL
   * @param query
   * @return
   */
  protected List<String> queryReturnSingleValues(Query query) {
    return  queryReturnSingleValues(query, SPARQL_VARIABLE_SUBJECT);
  }

  protected List<String> queryReturnSingleValues(Query query, String columnName) {
    return queryReturnSingleNodes(query, columnName).stream().map(RDFNode::toString).collect(Collectors.toList());
  }

  protected List<RDFNode> queryReturnSingleNodes(Query query, String columnName) {
    log.info("SPARQL query: \n" + query.toString());

    QueryExecution qExec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

    List<RDFNode> out = new ArrayList<>();
    ResultSet rs = qExec.execSelect();
    while (rs.hasNext()) {
      QuerySolution qs = rs.next();
      RDFNode columnNode = qs.get(columnName);
      out.add(columnNode);
    }
    return out;
  }

  protected List<String> queryForLabel(Query sparqlQuery, String resourceURI) throws KBProxyException {
    // Query all labels of the resource.
    List<RDFNode> nodes = queryReturnSingleNodes(sparqlQuery, SPARQL_VARIABLE_OBJECT);
    List<Label> labels = nodes.stream()
            .filter(item -> item.isLiteral() && !isNullOrEmpty(item.toString()))
            .map(node -> new Label(node.asLiteral().getString(), node.asLiteral().getLanguage()))
            .collect(Collectors.toList());

    List<String> filteredLabels;

    // Filter language tags
    String suffix = kbDefinition.getLanguageSuffix();
    if (!isNullOrEmpty(suffix)) {
      if (suffix.startsWith("@")) {
        suffix = suffix.substring(1);
      }
      final String finalSuffix = suffix;

      filteredLabels = labels.stream()
              .filter(item -> isNullOrEmpty(item.languageSuffix) || item.languageSuffix.equals(finalSuffix))
              .map(item -> item.labelValue)
              .collect(Collectors.toList());
    }
    else {
      filteredLabels = labels.stream()
              .map(item -> item.labelValue)
              .collect(Collectors.toList());
    }

    // The resource has no statement with label property, apply simple heuristics to parse the
    // resource URI.
    if (labels.size() == 0) {
      // URI like https://www.w3.org/1999/02/22-rdf-syntax-ns#type
      int trimPosition = resourceURI.lastIndexOf("#");

      // URI like http://dbpedia.org/property/name
      if (trimPosition == -1) {
        trimPosition = resourceURI.lastIndexOf("/");
      }

      if (trimPosition != -1) {
        // Remove anything that is not a character or digit
        // TODO: For a future improvement, take into account the "_" character.
        String stringValue = resourceURI.substring(trimPosition + 1).replaceAll("[^a-zA-Z0-9]", "").trim();

        // Derived KBs can have custom URI conventions.
        stringValue = applyCustomUriHeuristics(resourceURI, stringValue);
        stringValue = StringUtils.splitCamelCase(stringValue);

        filteredLabels.add(stringValue);
      }
    }

    return filteredLabels;
  }

  protected String applyCustomUriHeuristics(String resourceURI, String label) {
    return label;
  }

  /**
   * Compares the similarity of the object value in the pair (containing the label obtained from the KB) of certain resource (entity) and the cell value text (original label).
   * Then, it also sorts the list of candidates based on the scores.
   *
   * @param candidates
   * @param originalQueryLabel
   */
  protected void rank(List<Pair<String, String>> candidates, String originalQueryLabel) {
    final Map<Pair<String, String>, Double> scores = new HashMap<>();
    for (Pair<String, String> p : candidates) {
      String label = p.getValue();
      double s = stringMetric.compare(label, originalQueryLabel);
      scores.put(p, s);
    }

    candidates.sort((o1, o2) -> {
      Double s1 = scores.get(o1);
      Double s2 = scores.get(o2);
      return s2.compareTo(s1);
    });
  }

  @Override
  public List<String> getPropertyDomains(String uri) throws KBProxyException{
    return getPropertyValues(uri, kbDefinition.getStructureDomain());
  }

  @Override
  public List<String> getPropertyRanges(String uri) throws KBProxyException{
    return getPropertyValues(uri, kbDefinition.getStructureRange());
  }

  private List<String> getPropertyValues(String uri, String propertyUri) throws KBProxyException {
    Asserts.notBlank(uri, "uri");
    Asserts.notBlank(propertyUri, "propertyUri");

    String queryCache = createSolrCacheQuery_getPropertyValues(uri, propertyUri);
    return retrieveOrTryExecute(queryCache, cacheEntity, () -> {
      SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_OBJECT)
              .addWhere(createSPARQLResource(uri), createSPARQLResource(propertyUri), SPARQL_VARIABLE_OBJECT);

      return queryReturnSingleValues(builder.build(), SPARQL_VARIABLE_OBJECT);
    });
  }

  @Override
  public List<Entity> findResourceByFulltext(String pattern, int limit) {
    try {
      // Proposed resources can have types, that are not classes.
      return findByFulltext(() -> createExactMatchQueryForResources(pattern, limit, false), () -> createFulltextQueryForResources(pattern, limit, false), pattern);
    }
    catch (Exception e){
      // If the search expression causes any error on the KB side, we only log
      // the exception and return no results. The error is very likely due to
      // fulltext search requirements defined by the KB.
      log.error("Unexpected exception during resource search.", e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<Entity> findClassByFulltext(String pattern, int limit) {
    try {
      return findByFulltext(() -> createExactMatchQueryForClasses(pattern, limit), () -> createFulltextQueryForClasses(pattern, limit), pattern);
    }
    catch (Exception e){
      // If the search expression causes any error on the KB side, we only log
      // the exception and return no results. The error is very likely due to
      // fulltext search requirements defined by the KB.
      log.error("Unexpected exception during class search.", e);
      return new ArrayList<>();
    }
  }

  @Override
  public List<Entity> findPredicateByFulltext(String pattern, int limit, URI domain, URI range) {
    String domainString = domain != null ? domain.toString() : null;
    String rangeString = range != null ? range.toString() : null;

    try {
      return findByFulltext(() -> createExactMatchQueryForPredicates(pattern, limit, domainString, rangeString), () -> createFulltextQueryForPredicates(pattern, limit, domainString, rangeString), pattern);
    }
    catch (Exception e){
      // If the search expression causes any error on the KB side, we only log
      // the exception and return no results. The error is very likely due to
      // fulltext search requirements defined by the KB.
      log.error("Unexpected exception during predicate search.", e);
      return new ArrayList<>();
    }
  }

  @Override
  protected List<Entity> findEntityCandidatesInternal(String content) throws KBProxyException {
    return queryEntityCandidates(content);
  }

  @Override
  protected List<Entity> findEntityCandidatesOfTypesInternal(String content, String... types) throws KBProxyException {
    return queryEntityCandidates(content, types);
  }

  @Override
  protected Entity loadEntityInternal(String uri) throws KBProxyException {
    //prepare cache
    String queryCache = createSolrCacheQuery_loadResource(uri);
    Entity result = retrieveOrTryExecute(queryCache, cacheEntity, () -> {
      //there is nothing suitable in the cache or the cache is disabled
      AskBuilder builder = getAskBuilder().addWhere(createSPARQLResource(uri), createSPARQLResource(kbDefinition.getStructureInstanceOf()), "?Type");
      boolean askResult = ask(builder.build());

      if (!askResult) {
        return null;
      }

      String label = getResourceLabel(uri);
      Entity res = new Entity(uri, label);
      loadEntityAttributes(res);

      return res;
    });

    filterEntityTypes(result);
    return result;
  }

  @Override
  public boolean isInsertSupported() {
    return kbDefinition.isInsertSupported();
  }

  @Override
  public Entity insertClass(URI uri, String label, Collection<String> alternativeLabels, String superClass) throws KBProxyException {
    performInsertChecks(label);

    String url = checkOrGenerateUrl(kbDefinition.getInsertSchemaElementPrefix(), uri);

    if (isNullOrEmpty(superClass)){
      superClass = kbDefinition.getInsertRootClass();
    }

    StringBuilder tripleDefinition = createTripleDefinitionBase(url, label);
    appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);
    appendValue(tripleDefinition, kbDefinition.getInsertSubclassOf(), superClass, false);
    appendValue(tripleDefinition, kbDefinition.getStructureInstanceOf(), kbDefinition.getInsertClassType(), false);

    insert(tripleDefinition.toString());
    return new Entity(url, label);
  }

  @Override
  public Entity insertConcept(URI uri, String label, Collection<String> alternativeLabels, Collection<String> classes) throws KBProxyException {
    performInsertChecks(label);

    String url = checkOrGenerateUrl(kbDefinition.getInsertDataElementPrefix(), uri);

    StringBuilder tripleDefinition = createTripleDefinitionBase(url, label);
    appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);
    boolean typeSpecified = appendCollection(tripleDefinition, kbDefinition.getStructureInstanceOf(), classes, false);
    if (!typeSpecified){
      appendValue(tripleDefinition, kbDefinition.getStructureInstanceOf(), kbDefinition.getInsertRootClass(), false);
    }

    insert(tripleDefinition.toString());
    return new Entity(url, label);
  }

  @Override
  public Entity insertProperty(URI uri, String label, Collection<String> alternativeLabels, String superProperty, String domain, String range) throws KBProxyException {
    performInsertChecks(label);

    String url = checkOrGenerateUrl(kbDefinition.getInsertSchemaElementPrefix(), uri);

    StringBuilder tripleDefinition = createTripleDefinitionBase(url, label);
    appendCollection(tripleDefinition, kbDefinition.getInsertAlternativeLabel(), alternativeLabels, true);
    appendValue(tripleDefinition, kbDefinition.getStructureInstanceOf(), kbDefinition.getInsertPropertyType(), false);

    appendValueIfNotEmpty(tripleDefinition, kbDefinition.getInsertSubProperty(), superProperty, false);
    appendValueIfNotEmpty(tripleDefinition, kbDefinition.getStructureDomain(), domain, false);
    appendValueIfNotEmpty(tripleDefinition, kbDefinition.getStructureRange(), range, false);

    insert(tripleDefinition.toString());
    return new Entity(url, label);
  }

  private List<Entity> findByFulltext(QueryGetter exactQueryGetter, QueryGetter fulltextQueryGetter, String content) throws SolrServerException, ClassNotFoundException, IOException, KBProxyException, ParseException {
    // Find results by both fulltext and exact match
    Query exactQuery = exactQueryGetter.getQuery();
    List<String> exactQueryResult = queryReturnSingleValues(exactQuery);

    Query fulltextQuery = fulltextQueryGetter.getQuery();
    List<Pair<RDFNode, RDFNode>> fulltextQueryResult = queryReturnNodeTuples(fulltextQuery);

    // Marge results and prefer the exact match.
    Map<String, Entity> result = exactQueryResult.stream().collect(Collectors.toMap(item -> item, item -> new Entity(item, content)));

    for (Pair<RDFNode, RDFNode> fulltextResult : fulltextQueryResult) {
      String uri = fulltextResult.getKey().toString();
      if (!result.containsKey(uri)) {
        RDFNode labelNode = fulltextResult.getValue();

        String label = null;
        if (labelNode.isLiteral()) {
          label = labelNode.asLiteral().getString();
        }

        result.put(uri, new Entity(uri, isNullOrEmpty(label) ? content : label));
      }
    }

    return result.values().stream().collect(Collectors.toList());
  }


  protected void performInsertChecks(String label) throws KBProxyException {
    if (!isInsertSupported()){
      throw new KBProxyException("Insertion of is not supported for the " + kbDefinition.getName() + " knowledge base.");
    }

    if (isNullOrEmpty(label)){
      throw new KBProxyException("Label must not be empty.");
    }
  }

  private boolean appendCollection(StringBuilder tripleDefinition, String predicate, Collection<String> values, boolean isLiteral) {
    if (values == null) {
      return false;
    }

    boolean valueAppended = false;
    for (String value : values){
      if (isNullOrEmpty(value)){
        continue;
      }

      valueAppended = true;
      appendValue(tripleDefinition, predicate, value, isLiteral);
    }

    return  valueAppended;
  }

  private void appendValueIfNotEmpty(StringBuilder tripleDefinition, String predicate, String value, boolean isLiteral) {
    if (!isNullOrEmpty(value)) {
      appendValue(tripleDefinition, predicate, value, isLiteral);
    }
  }

  private void appendValue(StringBuilder tripleDefinition, String predicate, String value, boolean isLiteral) {
    tripleDefinition.append(" ; <");
    tripleDefinition.append(predicate);

    if (isLiteral){
      tripleDefinition.append("> ");
      tripleDefinition.append(createSPARQLLiteral(value, true));
    }
    else {
      tripleDefinition.append("> <");
      tripleDefinition.append(value);
      tripleDefinition.append(">");
    }
  }

  private StringBuilder createTripleDefinitionBase(String url, String label){
    StringBuilder tripleDefinition = new StringBuilder("<");
    tripleDefinition.append(url);
    tripleDefinition.append("> <");
    tripleDefinition.append(kbDefinition.getInsertLabel());
    tripleDefinition.append("> ");
    tripleDefinition.append(createSPARQLLiteral(label, true));

    return tripleDefinition;
  }

  private void insert(String tripleDefinition) {
    StringBuilder queryBuilder = new StringBuilder();

    if (prefixToUriMap != null) {
      for(Map.Entry<String, String> prefix : prefixToUriMap.entrySet()) {
        queryBuilder.append(String.format(SPARQL_PREFIX, prefix.getKey(), prefix.getValue()));
        queryBuilder.append("\n");
      }
    }

    queryBuilder.append(String.format(INSERT_BASE, kbDefinition.getInsertGraph(), tripleDefinition));
    String sparqlQuery = queryBuilder.toString();
    log.info("SPARQL query: \n" + sparqlQuery);

    UpdateRequest query = UpdateFactory.create(sparqlQuery);
    UpdateProcessor queryExecution = UpdateExecutionFactory.createRemote(query, kbDefinition.getSparqlEndpoint());

    queryExecution.execute();
  }

  protected String checkOrGenerateUrl(URI baseURI, URI uri) throws KBProxyException {
    if (uri == null) {
      return combineURI(baseURI, UUID.randomUUID().toString());
    } else {
      String uriString;
      if (uri.isAbsolute()) {
        uriString = uri.toString();
      }
      else {
        uriString = combineURI(baseURI, uri.toString());
      }

      AskBuilder builder = getAskBuilder().addWhere(createSPARQLResource(uriString), SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT);
      Query query = builder.build();

      boolean exists = ask(query);
      if (exists) {
        throw new IllegalArgumentException("The knowledge base " + kbDefinition.getName() + " already contains a resource with url: " + uriString);
      }

      return uriString;
    }
  }

  private String combineURI(URI baseUri, String uri) {
    String baseString = baseUri.toString();
    if (baseString.charAt(baseString.length() - 1) == '/') {
      return baseString + uri;
    }
    else {
      return baseString + "/" + uri;
    }
  }

  protected boolean isNullOrEmpty(String string){
    return string == null || string.isEmpty();
  }

  @SuppressWarnings("unchecked")
  private List<Entity> queryEntityCandidates(final String content, String... types)
      throws KBProxyException {

    //prepare cache
    String queryCache = createSolrCacheQuery_findResources(content, types);

    List<Entity> result = retrieveOrTryExecute(queryCache, cacheEntity, () -> {
      // adjust the content string before query is executed
      String unescapedContent = StringEscapeUtils.unescapeXml(content);
      int bracket = unescapedContent.indexOf("(");
      if (bracket != -1) {
        unescapedContent = unescapedContent.substring(0, bracket).trim();
      }
      if (StringUtils.toAlphaNumericWhitechar(unescapedContent).trim().length() == 0)
        return new ArrayList<Entity>();

      List<Entity> res = new ArrayList<>();
      List<Pair<String, String>> queryResult = new ArrayList<>();

      //1. try exact string
      // prepare the query
      Query sparqlQuery = createExactMatchQueryForResources(unescapedContent,null, false, types);
      List<String> resourceAndType = queryReturnSingleValues(sparqlQuery);
      for(String resource : resourceAndType) {
        queryResult.add(new Pair<>(resource, unescapedContent)); //I may add content because it is exact match
      }

      //2. if result is empty, try regex (if allowed)
      //the same for type/non-type restrictions ??
      if (resourceAndType.size() == 0 && fuzzyKeywords) {
        log.debug("(query by regex. This can take a long time)");
        sparqlQuery = createFulltextQueryForResources(unescapedContent, null, false,  types);
        queryResult = queryReturnTuples(sparqlQuery, unescapedContent);
      }

      //3. rank result by the degree of matches.
      rank(queryResult, unescapedContent);

      //get all attributes for the candidates, set also types (based on the predicates which may contain type)
      log.debug("(DB QUERY =" + queryResult.size() + " results)");
      for (Pair<String, String> candidate : queryResult) {
        //Next get attributes for each topic
        String label = candidate.getValue();
        if (label == null)
          label = unescapedContent;
        Entity ec = new Entity(candidate.getKey(), label);
        loadEntityAttributes(ec);
        res.add(ec);
      }

      return res;
    });

    //filter entity's clazz, and attributes
    for (Entity ec : result) {
      filterEntityTypes(ec);
    }

    return result;
  }

  private void loadEntityAttributes(Entity ec) throws KBProxyException {
    List<Attribute> attributes = findAttributesOfEntitiesInternal(ec);
    ec.setAttributes(attributes);
    for (Attribute attr : attributes) {
      adjustValueOfURLResource(attr);
      if (KBProxyUtils.contains(kbDefinition.getPredicateType(), attr.getRelationURI()) &&
          !ec.hasType(attr.getValueURI())) {
        ec.addType(new Clazz(attr.getValueURI(), attr.getValue()));
      }
    }
  }

  private void filterEntityTypes(Entity entity) {
    List<Clazz> filteredTypes = resultFilter.filterClazz(entity.getTypes());

    entity.clearTypes();
    for (Clazz ft : filteredTypes) {
      entity.addType(ft);
    }
  }

  @SuppressWarnings("unchecked")
  private void adjustValueOfURLResource(Attribute attr) throws KBProxyException {
    // TODO: This is a mess, refactor in #256.
    String valueLabel = getResourceLabel(attr.getValue());
    String relationLabel = getResourceLabel(attr.getRelationURI());

    attr.setValueURI(attr.getValue());
    attr.setValue(valueLabel);
    attr.setRelationLabel(relationLabel);
  }

  @SuppressWarnings("unchecked")
  private String getResourceLabel(String uri) throws KBProxyException {
    if (uri.startsWith("http")) {
      String queryCache = createSolrCacheQuery_findLabelForResource(uri);

      List<String> result = retrieveOrTryExecute(queryCache, cacheEntity, () -> {
        Query sparqlQuery = createGetLabelQuery(uri);
        return queryForLabel(sparqlQuery, uri);
      });

      if (result.size() > 0) {
        return result.get(0);
      }
    }

    return uri;
  }

  @Override
  protected List<Attribute> findAttributesOfEntitiesInternal(Entity ec) throws KBProxyException {
    return findAttributes(ec.getId(), cacheEntity);
  }

  @SuppressWarnings("unchecked")
  private List<Attribute> findAttributes(String id, SolrCache cache) throws KBProxyException {
    if (id.length() == 0)
      return new ArrayList<>();

    String queryCache = createSolrCacheQuery_findAttributesOfResource(id);
    List<Attribute> result = retrieveOrTryExecute(queryCache, cache, () -> {
      List<Attribute> res = new ArrayList<>();

      SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT)
              .addWhere(createSPARQLResource(id), SPARQL_VARIABLE_PREDICATE, SPARQL_VARIABLE_OBJECT);

      Query query = builder.build();
      log.info("SPARQL query: \n" + query.toString());

      QueryExecution qExec = QueryExecutionFactory.sparqlService(kbDefinition.getSparqlEndpoint(), query);

      ResultSet rs = qExec.execSelect();
      while (rs.hasNext()) {
        QuerySolution qs = rs.next();
        RDFNode predicate = qs.get(SPARQL_VARIABLE_PREDICATE);
        RDFNode object = qs.get(SPARQL_VARIABLE_OBJECT);
        if (object != null) {
          Attribute attr = new SPARQLAttribute(predicate.toString(), object.toString());
          res.add(attr);
        }
      }

      return res;
    });

    //filtering
    result = resultFilter.filterAttribute(result);
    return result;
  }

  @Override
  protected List<Attribute> findAttributesOfClazzInternal(String clazzId) throws KBProxyException {
    return findAttributes(clazzId, cacheEntity);
  }

  @Override
  protected List<Attribute> findAttributesOfPropertyInternal(String propertyId) throws KBProxyException {
    return findAttributes(propertyId, cacheEntity);
  }

  protected String createSolrCacheQuery_findLabelForResource(String url) {
    return "LABEL_" + url;
  }

  private SelectBuilder createFulltextQueryBuilder(String content, Integer limit, String... types) throws ParseException {
    SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_SUBJECT, SPARQL_VARIABLE_OBJECT);

    if (limit != null){
      builder = builder.setLimit(limit);
    }

    // Label conditions
    builder = addUnion(
            builder,
            kbDefinition.getPredicateLabel(),
            (subBuilder, value) -> {
              subBuilder = subBuilder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(value), SPARQL_VARIABLE_OBJECT);

              if (kbDefinition.getUseBifContains()) {
                subBuilder = subBuilder.addWhere(SPARQL_VARIABLE_OBJECT, SPARQL_PREDICATE_BIF_CONTAINS, createFulltextExpression(content));
              }

              return subBuilder;
            },
            SPARQL_VARIABLE_SUBJECT, SPARQL_VARIABLE_OBJECT);

    if (!kbDefinition.getUseBifContains()) {
      String regexFilter = String.format(SPARQL_FILTER_REGEX, SPARQL_VARIABLE_OBJECT, createSPARQLLiteral(content));
      builder = builder.addFilter(regexFilter);
    }

    // Types restriction
    builder = addTypeRestriction(builder, Arrays.asList(types));

    return builder;
  }

  private SelectBuilder createExactMatchQueryBuilder(String content, Integer limit, String... types) throws ParseException {
    SelectBuilder builder = getSelectBuilder(SPARQL_VARIABLE_SUBJECT);

    if (limit != null){
      builder = builder.setLimit(limit);
    }

    // Label restriction
    builder = addLabelRestriction(builder, content);

    // Type restriction
    builder = addTypeRestriction(builder, Arrays.asList(types));

    return builder;
  }

  /**
   * Strips the searched value of all special characters and creates a proper search expression from the individual words.
   **/
  private String createFulltextExpression(String content) {
    StringBuilder result = new StringBuilder("'");

    boolean wordStarted = false;
    for (Character character : content.toCharArray()) {
      if (Character.isLetterOrDigit(character)) {
        if (!wordStarted) {
          if(result.length() > 1) {
            result.append(" AND ");
          }
          result.append("\"");
          wordStarted = true;
        }

        result.append(character);
      }
      else {
        if (wordStarted) {
          result.append("\"");
          wordStarted = false;
        }
      }
    }

    if (wordStarted) {
      result.append("\"");
    }

    result.append("'");
    return result.toString();
  }

  private SelectBuilder addLabelRestriction(SelectBuilder builder, String content) {
    return addUnion(
            builder,
            kbDefinition.getPredicateLabel(),
            (subBuilder, value) -> subBuilder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(value), createSPARQLLiteral(content, true)),
            SPARQL_VARIABLE_SUBJECT);
  }

  private SelectBuilder addTypeRestriction(SelectBuilder builder, Collection<String> types) {
    return addUnion(
            builder,
            types,
            (subBuilder, value) -> subBuilder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(kbDefinition.getStructureInstanceOf()), createSPARQLResource(value)),
            SPARQL_VARIABLE_SUBJECT);
  }

  private SelectBuilder addClassRestriction(SelectBuilder builder, boolean restrictClassTypes) {

      if (kbDefinition.getSearchClassTypeMode().equals(KBDefinition.SEARCH_CLASS_TYPE_MODE_VALUE.INDIRECT)) {
        //as proposed by Jan
        builder = builder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(kbDefinition.getStructureInstanceOf()), SPARQL_VARIABLE_CLASS);
        // If there are no class types defined, then we at least demand the subject is an instance of some class.
        if (restrictClassTypes && kbDefinition.getStructureClass().size() > 0) {
          builder = addUnion(
                  builder,
                  kbDefinition.getStructureClass(),
                  (subBuilder, value) -> subBuilder.addWhere(SPARQL_VARIABLE_CLASS, createSPARQLResource(kbDefinition.getStructureInstanceOf()), createSPARQLResource(value)),
                  SPARQL_VARIABLE_CLASS);
        }
      }
      else {
        if (restrictClassTypes && kbDefinition.getStructureClass().size() > 0) {
          //kbDefinition.getSearchClassTypeMode().equals(SEARCH_CLASS_TYPE_MODE_VALUE.DIRECT)
          builder = addUnion(
                  builder,
                  kbDefinition.getStructureClass(),
                  (subBuilder, value) -> subBuilder.addWhere(SPARQL_VARIABLE_SUBJECT, createSPARQLResource(kbDefinition.getStructureInstanceOf()), createSPARQLResource(value)),
                  SPARQL_VARIABLE_SUBJECT);
        }
      }

    return builder;
  }

  private SelectBuilder getSelectBuilder(String... variables) {
    SelectBuilder builder = new SelectBuilder();

    builder = builder.setDistinct(true);

    if (prefixToUriMap != null) {
      for (Map.Entry<String, String> prefix : prefixToUriMap.entrySet()) {
        builder = builder.addPrefix(prefix.getKey(), prefix.getValue());
      }
    }

    for (String variable : variables) {
      builder = builder.addVar(variable);
    }

    return builder;
  }
  private AskBuilder getAskBuilder() {
    AskBuilder builder = new AskBuilder();

    if (prefixToUriMap != null) {
      for (Map.Entry<String, String> prefix : prefixToUriMap.entrySet()) {
        builder = builder.addPrefix(prefix.getKey(), prefix.getValue());
      }
    }

    return builder;
  }

  private static String createSPARQLResource(String url) {
    return String.format(SPARQL_RESOURCE, url);
  }

  private String createSPARQLLiteral(String value, boolean addLanguageSuffix) {
    String result = String.format(SPARQL_STRING_LITERAL, escapeSPARQLLiteral(value));

    if (addLanguageSuffix){
      result += kbDefinition.getLanguageSuffix();
    }

    return result;
  }

  private String createSPARQLLiteral(String value) {
    return  createSPARQLLiteral(value, false);
  }

  private static String escapeSPARQLLiteral(String value){
    StringBuilder builder = new StringBuilder(value);

    for(int index = builder.length() - 1; index >= 0; index--) {
      String replacement = SPARQL_ESCAPE_REPLACEMENTS.get(value.charAt(index));

      if (replacement != null) {
        builder.deleteCharAt(index);
        builder.insert(index, replacement);
      }
    }

    return builder.toString();
  }

  private SelectBuilder addUnion(SelectBuilder builder, Collection<String> values, BuilderAction action, String... variables) {
    if (values.size() == 0) {
      return builder;
    }

    if (values.size() == 1) {
      return action.performAction(builder, values.iterator().next());
    }

    SelectBuilder subBuilder = getSelectBuilder(variables);

    for (String value : values) {
      SelectBuilder unionBuilder = new SelectBuilder();
      unionBuilder = action.performAction(unionBuilder, value);
      subBuilder = subBuilder.addUnion(unionBuilder);
    }

    return builder.addSubQuery(subBuilder);
  }

  private interface QueryGetter {
    Query getQuery() throws KBProxyException, ParseException;
  }

  private interface BuilderAction {
    SelectBuilder performAction(SelectBuilder builder, String value);
  }

  private class Label {
    String labelValue;
    String languageSuffix;

    Label(String labelValue, String languageSuffix) {
      this.labelValue = labelValue;
      this.languageSuffix = languageSuffix;
    }
  }
}
