package uk.ac.shef.dcs.kbproxy.solr;

import java.util.Arrays;
import java.util.stream.Collectors;

/*
 * createSolrCacheQuery_XXX defines how a solr query should be constructed. If your implementing
 * class want to benefit from solr cache, you should call these methods to generate a query
 * string, which will be considered as the id of a record in the solr index. that query will be
 * performed, to attempt to retrieve previously saved results if any.
 *
 * If there are no previously cached results, you have to perform your remote call to the KB,
 * obtain the results, then cache the results in solr. Again you should call these methods to
 * create a query string, which should be passed as the id of the record to be added to solr
 */
public class KbProxySolr {

  public KbProxySolr() {
    // TODO Auto-generated constructor stub
  }

  public static String createSolrCacheQuery_findAttributesOfResource(final String resource) {
    return "ATTR_" + resource;
  }

  public static String createSolrCacheQuery_findResources(final String content, final String... types) {
    final StringBuilder builder = new StringBuilder("FIND_RESOURCE_");
    builder.append(content);
  
    for (final String type : Arrays.stream(types).sorted().collect(Collectors.toList())) {
      builder.append("_TYPE_");
      builder.append(type);
    }
  
    return builder.toString();
  }

  public static String createSolrCacheQuery_getPropertyValues(final String uri,
      final String propertyUri) {
    return "GET_PROPERTY_VALUES_" + uri + "_" + propertyUri;
  }

  public static String createSolrCacheQuery_loadResource(final String uri) {
    return "LOAD_RESOURCE_" + uri;
  }

}
