package uk.ac.shef.dcs.sti.core.scorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simmetrics.StringMetric;

import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.util.Pair;
import uk.ac.shef.dcs.util.StringUtils;

/**
 * Created by - on 02/04/2016.
 */
public class AttributeValueMatcher {

  protected List<String> stopWords;
  protected double minScoreThreshold;
  protected StringMetric stringMetric;

  public AttributeValueMatcher(final double minScoreThreshold, final List<String> stopWords,
      final StringMetric stringMetric) {
    this.minScoreThreshold = minScoreThreshold;
    this.stopWords = stopWords;
    this.stringMetric = stringMetric;
  }

  protected Map<Integer, DataTypeClassifier.DataType> classifyAttributeValueDataType(
      final List<Attribute> attributes) {
    final Map<Integer, DataTypeClassifier.DataType> dataTypes = new HashMap<>();
    // typing the objects of facts
    for (int index = 0; index < attributes.size(); index++) {
      final Attribute fact = attributes.get(index);
      final String val = fact.getValue();
      final String id_of_val = fact.getValueURI();

      if (id_of_val != null) {
        dataTypes.put(index, DataTypeClassifier.DataType.NAMED_ENTITY);
      } else {
        final DataTypeClassifier.DataType type = DataTypeClassifier.classify(val);
        dataTypes.put(index, type);
      }
    }
    return dataTypes;

  }

  protected boolean isValidType(final DataTypeClassifier.DataType dataType) {
    if (dataType.equals(DataTypeClassifier.DataType.ORDERED_NUMBER)) {
      return false;
    }
    if (dataType.equals(DataTypeClassifier.DataType.EMPTY)) {
      return false;
    }
    if (dataType.equals(DataTypeClassifier.DataType.LONG_TEXT)) {
      return false;
    }
    return true;
  }

  public Map<Integer, List<Pair<Attribute, Double>>> match(final List<Attribute> attributes,
      final Map<Integer, String> cellTextValues,
      final Map<Integer, DataTypeClassifier.DataType> columnDataTypes) {
    final Map<Integer, List<Pair<Attribute, Double>>> matchedScores = new HashMap<>();

    // check the data type of attributes' values
    final Map<Integer, DataTypeClassifier.DataType> attributeValueDataTypes =
        classifyAttributeValueDataType(attributes);

    // compute scores for each value on the row
    for (final Map.Entry<Integer, String> e : cellTextValues.entrySet()) {
      final int column = e.getKey();
      final String textValue = e.getValue();
      final DataTypeClassifier.DataType cellDataType = columnDataTypes.get(column);
      if ((cellDataType == null) || !isValidType(cellDataType)) {
        continue;
      }

      double maxScore = 0.0;
      final Map<Integer, Double> attrIndex_to_matchScores = new HashMap<>();
      for (int index = 0; index < attributes.size(); index++) {
        final DataTypeClassifier.DataType dataTypeOfAttrValue = attributeValueDataTypes.get(index);
        final Attribute attr = attributes.get(index);
        if (!isValidType(dataTypeOfAttrValue)) {
          continue;
        }

        final double score =
            score(textValue, cellDataType, attr.getValue(), dataTypeOfAttrValue, this.stopWords);
        if (score > maxScore) {
          maxScore = score;
        }
        attrIndex_to_matchScores.put(index, score);
      }


      if ((maxScore != 0) && (maxScore >= this.minScoreThreshold)) {
        final List<Pair<Attribute, Double>> list = new ArrayList<>();
        for (final Map.Entry<Integer, Double> entry : attrIndex_to_matchScores.entrySet()) {
          if (entry.getValue() == maxScore) {
            final Attribute winningAttr = attributes.get(entry.getKey());
            final Pair<Attribute, Double> score_obj = new Pair<>(winningAttr, maxScore);
            list.add(score_obj);
          }
        }
        if (list.size() > 0) {
          matchedScores.put(column, list);
        }

      }

    }
    return matchedScores;
  }


  protected double matchNumber(final String string1, final String string2) {
    try {
      final double number1 = Double.valueOf(string1);
      final double number2 = Double.valueOf(string2);

      final double max = Math.max(number1, number2);
      final double maxDiff = max * 0.05; // the maximum difference allowed between the two numbers
                                         // in order to mean they are equal is 10% of the max number
      final double diff = Math.abs(number1 - number2);

      if (diff < maxDiff) {
        return 1.0;
      } else {
        return maxDiff / diff;
      }
    } catch (final Exception e) {
      return -1.0;
    }
  }

  protected double matchText(String target, String base, final Collection<String> stopWords) {
    // method 1, check how much overlap the two texts have
    target = StringUtils.toAlphaNumericWhitechar(target);
    base = StringUtils.toAlphaNumericWhitechar(base);
    final Set<String> target_toks = new HashSet<>(
        StringUtils.toBagOfWords(target, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
    target_toks.removeAll(stopWords);
    final Set<String> base_toks = new HashSet<>(
        StringUtils.toBagOfWords(base, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
    base_toks.removeAll(stopWords);

    // method 2
    final double score = CollectionUtils.computeDice(target_toks, base_toks);
    return score;
  }

  /**
   * number match scores are computed by matchNumber; text match scores are computed by dice; long
   * string (urls) are computed by a string similarity metric
   *
   * @param string1
   * @param type_of_string1
   * @param string2
   * @param type_of_string2
   * @param stopWords
   * @return
   */
  protected double score(String string1, final DataTypeClassifier.DataType type_of_string1,
      String string2, final DataTypeClassifier.DataType type_of_string2,
      final Collection<String> stopWords) {
    if (type_of_string1.equals(DataTypeClassifier.DataType.NAMED_ENTITY)
        && (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER)
            || type_of_string2.equals(DataTypeClassifier.DataType.DATE))) {
      return 0.0;
    }
    if (type_of_string2.equals(DataTypeClassifier.DataType.NAMED_ENTITY)
        && (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER)
            || type_of_string1.equals(DataTypeClassifier.DataType.DATE))) {
      return 0.0;
    }
    // long string like URL
    if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING)
        && type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING)) {
      string1 = StringUtils.toAlphaNumericWhitechar(string1);
      string2 = StringUtils.toAlphaNumericWhitechar(string2);
      return this.stringMetric.compare(string1, string2);
    }
    if (type_of_string1.equals(DataTypeClassifier.DataType.LONG_STRING)
        || type_of_string2.equals(DataTypeClassifier.DataType.LONG_STRING)) {
      return 0.0;
    }

    // number
    double score = -1.0;
    if (type_of_string1.equals(DataTypeClassifier.DataType.NUMBER)
        && (type_of_string2.equals(DataTypeClassifier.DataType.NUMBER))) {
      score = matchNumber(string1, string2);
    }

    if (score == -1) {
      score = matchText(string1, string2, stopWords);
    }
    return score == -1.0 ? 0.0 : score;
  }

}
