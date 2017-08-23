package uk.ac.shef.dcs.kbproxy.sparql.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectBuilder {
  private static final String SPARQL_RESOURCE = "<%1$s>";

  private boolean isDistinct = false;
  private Integer limit = null;
  private Map<String, String> prefixes = new HashMap<>();
  private Set<String> variables = new HashSet<>();
  private List<SelectBuilder> unions = new ArrayList<>();
  private List<SelectBuilder> subExpressions = new ArrayList<>();
  private List<WhereExpression> whereExpressions = new ArrayList<>();
  private List<String> filters = new ArrayList<>();

  public SelectBuilder setDistinct(boolean isDistinct) {
    this.isDistinct = isDistinct;
    return this;
  }

  public SelectBuilder setLimit(Integer limit) {
    this.limit = limit;
    return null;
  }

  public SelectBuilder addPrefix(String key, String value) {
    prefixes.put(key, value);
    return this;
  }

  public SelectBuilder addVar(String variable) {
    variables.add(variable);
    return this;
  }

  public SelectBuilder addUnion(SelectBuilder unionBuilder) {
    unions.add(unionBuilder);
    return this;
  }

  public SelectBuilder addSubExpression(SelectBuilder subBuilder) {
    subExpressions.add(subBuilder);
    return this;
  }

  public SelectBuilder addWhere(String subject, String predicate, String object) {
    whereExpressions.add(new WhereExpression(subject, predicate, object));
    return this;
  }

  public SelectBuilder addFilter(String filter) {
    filters.add(filter);
    return this;
  }

  public String build() {
    StringBuilder builder = new StringBuilder();

    prefixes.forEach((key, value) -> {
      builder.append("PREFIX ");
      builder.append(key);
      builder.append(": ");
      builder.append(String.format(SPARQL_RESOURCE, value));
      builder.append("\n");
    });

    builder.append("SELECT ");
    if(isDistinct) {
      builder.append("DISTINCT ");
    }
    if (variables.size() > 0) {
      variables.forEach(variable -> {
        builder.append(variable);
        builder.append(" ");
      });
    }
    else {
      builder.append("*");
    }
    builder.append("\n");

    buildWhere(builder, "", false);

    if (limit != null) {
      builder.append("LIMIT ");
      builder.append(limit);
    }

    return builder.toString();
  }

  private void buildWhere(StringBuilder builder, String prefix, boolean appendDot) {
    builder.append(prefix);
    builder.append("{");
    builder.append("\n");

    String innerPrefix = prefix + "  ";

    subExpressions.forEach(subBuilder -> subBuilder.buildWhere(builder, innerPrefix, true));

    for (int index = 0; index < unions.size(); index++){
      boolean isLast = index == (unions.size() - 1);

      unions.get(index).buildWhere(builder, innerPrefix, isLast);
      if (!isLast) {
        builder.append(innerPrefix);
        builder.append("UNION");
        builder.append("\n");
      }
    }

    whereExpressions.forEach(expression -> {
      builder.append(innerPrefix);
      builder.append(expression.subject);
      builder.append(" ");
      builder.append(expression.predicate);
      builder.append(" ");
      builder.append(expression.object);
      builder.append(" .");
      builder.append("\n");
    });

    filters.forEach(filter -> {
      builder.append(innerPrefix);
      builder.append("FILTER ");
      builder.append(filter);
      builder.append(" .");
      builder.append("\n");
    });

    builder.append(prefix);
    builder.append("}");
    if (appendDot) {
      builder.append(" .");
    }
    builder.append("\n");
  }

  private class WhereExpression {
    String subject;
    String predicate;
    String object;

    WhereExpression(String subject, String predicate, String object) {
      this.subject = subject;
      this.predicate = predicate;
      this.object = object;
    }
  }
}
