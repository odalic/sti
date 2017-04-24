package uk.ac.shef.dcs.sti.core.subjectcol;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.sti.nlp.TermFreqCounter;
import uk.ac.shef.dcs.util.Cache;
import uk.ac.shef.dcs.websearch.WebSearch;
import uk.ac.shef.dcs.websearch.WebSearchResultDoc;

/**
 */
public class WSScorer {
  protected static final double TITLE_WEIGHT_SCALAR = 2.0; // if a value is found in title of a
                                                           // search result document, it receives
                                                           // higher weight
  protected static final boolean WORD_ORDER_MATTERS = false;
  protected static final Logger LOG = LoggerFactory.getLogger(WSScorer.class.getName());
  protected Cache cache;
  protected WebSearch searcher;
  protected TermFreqCounter counter = new TermFreqCounter();

  protected List<String> stopWords;


  public WSScorer(final Cache cache, final WebSearch searcher, final List<String> stopWords) {
    this.cache = cache;
    this.searcher = searcher;
    this.stopWords = stopWords;
  }

  private double boostScoreByComposingTokens(final double score_to_increment_by_component,
      final String original) {
    final int length = original.split("\\s+").length;
    if (length < 2) {
      return 0;
    }
    return score_to_increment_by_component * (1.0 / (length/* * length */));
  }

  protected String createSolrCacheQuery(final String... values) {
    final StringBuilder sb = new StringBuilder();
    for (final String v : values) {
      sb.append(v).append(" ");
    }
    return sb.toString().trim();
  }

  @SuppressWarnings("unchecked")
  protected List<WebSearchResultDoc> findInCache(final String queryId)
      throws IOException, ClassNotFoundException, SolrServerException {
    return (List<WebSearchResultDoc>) this.cache.retrieve(queryId);
  }



  private boolean ignore(final String t) {
    if (this.stopWords.contains(t)) {
      return true;
    }
    try {
      Long.valueOf(t); // isValidAttribute numbers
      return true;
    } catch (final Exception e) {
    }
    return false;
  }

  protected String normalize(final String value) {
    return value.replaceAll("[^\\p{L}\\s\\d]", " ").replaceAll("\\s+", " ").trim().toLowerCase();
  }


  // remove short phrase's start offsets if it is part of a longer phrase
  protected void removeOverlapOffsets(final Set<Integer> shorterPhraseStarts,
      final Set<Integer> longerPhraseStarts, final int longerPhraseLength) {
    final Iterator<Integer> it = shorterPhraseStarts.iterator();
    while (it.hasNext()) {
      final Integer s = it.next();
      for (final int st : longerPhraseStarts) {
        if ((st <= s) && (s < (st + longerPhraseLength))) { // the shorter phrase's start is
                                                            // included in the longer phrase
                                                            // boundary
          it.remove();
          break;
        }
      }
    }
  }


  // take each normlised value, computeElementScores it against each search result document and sum
  // up the scores
  protected Map<String, Double> score(final List<WebSearchResultDoc> searchResult,
      final String... normalizedValues) {
    final Map<String, Double> scores = new HashMap<>();

    for (final WebSearchResultDoc doc : searchResult) {
      final String title = doc.getTitle();
      score(scores, title, TITLE_WEIGHT_SCALAR, normalizedValues);
      final String desc = doc.getDescription();
      score(scores, desc, 1.0, normalizedValues);
    }

    return scores;
  }

  private void score(final Map<String, Double> scores, String context,
      final double contextWeightScalar, final String... normalizedValues) {
    context = normalize(context);

    final Map<String, Set<Integer>> offsets = new HashMap<>();
    for (final String v : normalizedValues) {
      // v=normalize(v);
      if (v.length() < 1) {
        continue; // isValidAttribute 1 char tokens
      }
      final Set<Integer> os = this.counter.countOffsets(v, context);
      offsets.put(v, os);
    }

    final Map<String, Set<String>> phrase_to_composing_tokens = new HashMap<>();
    final Map<String, Set<Integer>> composing_token_offsets = new HashMap<>();
    // then the composing tokens for each normalizedValue
    for (final String v : normalizedValues) {
      final String[] toks = v.split("\\s+");
      if (toks.length == 1) {
        continue;
      }
      final Set<String> composing_toks = new HashSet<>();
      for (final String t : toks) {
        // t = t.trim();
        if (ignore(t)) {
          continue;
        }
        composing_toks.add(t);
        if (offsets.get(t) != null) {
          composing_token_offsets.put(t, offsets.get(t));
        } else if (composing_token_offsets.get(t) == null) {
          final Set<Integer> os = this.counter.countOffsets(t, context);
          composing_token_offsets.put(t, os);
        }
      }
      phrase_to_composing_tokens.put(v, composing_toks);
    }
    // discount double counting due to string inclusion between cells
    for (final String a : offsets.keySet()) {
      for (final String b : offsets.keySet()) {
        if (a.equals(b)) {
          continue;
        }
        final Set<Integer> offsets_a = offsets.get(a);
        final Set<Integer> offsets_b = offsets.get(b);
        if ((offsets_a.size() == 0) || (offsets_b.size() == 0)) {
          continue;
        }
        if (a.contains(b)) {
          removeOverlapOffsets(offsets.get(b), offsets.get(a), a.length());
          // offs ets.get(b).removeAll(offsets.get(a)); //string a contains b, so freq of b should
          // be decreased by the freq of a
        } else if (b.contains(a)) {
          removeOverlapOffsets(offsets.get(a), offsets.get(b), b.length());
          // offsets.get(a).removeAll(offsets.get(b));
        }
      }
    }
    // discount double counting due to string inclusion between a cell's value and its composing
    // tokens
    for (final String a : offsets.keySet()) {
      for (final String b : composing_token_offsets.keySet()) {
        if (a.equals(b)) {
          continue;
        }
        final Set<Integer> offsets_a = offsets.get(a);
        final Set<Integer> offsets_b = composing_token_offsets.get(b);
        if ((offsets_a.size() == 0) || (offsets_b == null) || (offsets_b.size() == 0)) {
          continue;
        }
        removeOverlapOffsets(offsets_b, offsets_a, a.length());
        // offs ets.get(b).removeAll(offsets.get(a)); //string a contains b, so freq of b should be
        // decreased by the freq of a
      }
    }

    // second of all, understand ordering
    final Map<String, Integer> firstOccurrenceOfPhrase = new HashMap<>();
    for (final Map.Entry<String, Set<Integer>> entry : offsets.entrySet()) {
      if (entry.getValue().size() == 0) {
        continue;
      }
      int min = Integer.MAX_VALUE;
      for (final Integer i : entry.getValue()) {
        if (i < min) {
          min = i;
        }
      }
      firstOccurrenceOfPhrase.put(entry.getKey(), min);
    }
    final Map<String, Integer> firstOccurrenceOfComposingTokens = new HashMap<>();
    for (final Map.Entry<String, Set<Integer>> entry : composing_token_offsets.entrySet()) {
      if (entry.getValue().size() == 0) {
        continue;
      }
      int min = Integer.MAX_VALUE;
      for (final Integer i : entry.getValue()) {
        if (i < min) {
          min = i;
        }
      }
      firstOccurrenceOfComposingTokens.put(entry.getKey(), min);
    }

    final List<String> original_phrases = new ArrayList<>(firstOccurrenceOfPhrase.keySet());
    Collections.sort(original_phrases, (o1, o2) -> new Integer(firstOccurrenceOfPhrase.get(o1))
        .compareTo(firstOccurrenceOfPhrase.get(o2)));

    final List<String> composing_tokens =
        new ArrayList<>(firstOccurrenceOfComposingTokens.keySet());
    Collections.sort(composing_tokens,
        (o1, o2) -> new Integer(firstOccurrenceOfComposingTokens.get(o1))
            .compareTo(firstOccurrenceOfComposingTokens.get(o2)));


    // last of all, compute scores
    original_phrases.size();
    final double order_scalar_composing_tokens = 1.0 / composing_tokens.size();

    for (int o = 0; o < original_phrases.size(); o++) {
      final String phrase = original_phrases.get(o);
      if (offsets.get(phrase) != null) {
        final int freq = offsets.get(phrase).size();
        if (freq > 0) { // exact cell value found
          // double ordering_weight_multiplier = (original_phrases.size() - o) *
          // ordering_multiplier_original;
          final double score_to_increment = offsets.get(phrase).size()
              * /* ordering_weight_multiplier */1.0 * contextWeightScalar;

          Double score = scores.get(phrase);
          score = score == null ? 0.0 : score;
          score = score + score_to_increment;
          scores.put(phrase, score);
        }
      }
    }
    for (int o = 0; o < composing_tokens.size(); o++) {
      final String candidate = composing_tokens.get(o);
      if (composing_token_offsets.get(candidate) != null) {
        final int freq = composing_token_offsets.get(candidate).size();
        if (freq > 0) { // exact cell value found
          double order_weight_scalar =
              (composing_tokens.size() - o) * order_scalar_composing_tokens;
          if (!WORD_ORDER_MATTERS) {
            order_weight_scalar = 1.0;
          }
          double score_to_increment = composing_token_offsets.get(candidate).size()
              * order_weight_scalar * contextWeightScalar;

          // find corresponding parent cell value
          for (final Map.Entry<String, Set<String>> e : phrase_to_composing_tokens.entrySet()) {
            if (e.getValue().contains(candidate)) {
              score_to_increment = boostScoreByComposingTokens(score_to_increment, e.getKey());
              Double score = scores.get(e.getKey());
              score = score == null ? 0.0 : score;
              score = score + score_to_increment;
              scores.put(e.getKey(), score);
            }
          }
        }
      }
    }

  }

  /**
   * Score each string value using WS
   *
   * @param values
   * @return
   * @throws APIKeysDepletedException
   * @throws IOException
   */
  public Map<String, Double> score(final String... values) throws IOException {
    final String queryId = createSolrCacheQuery(values);
    // 1. check cache
    List<WebSearchResultDoc> result = null;
    try {
      result = findInCache(queryId);
    } catch (final Exception e) {
      // e.printStackTrace();
    }

    // 2. if not in cache, perform web search, computeElementScores results, and cache results
    if (result == null/* ||result.size()==0 */) {
      final Date start = new Date();
      try {
        final InputStream is = this.searcher.search(queryId);
        List<WebSearchResultDoc> searchResult = null;
        try {
          searchResult = this.searcher.getResultParser().parse(is);
        } catch (final IllegalArgumentException e) {
          LOG.warn("The search stream is invalid!", e);
        }

        result = searchResult == null ? new ArrayList<>() : searchResult;

        this.cache.cache(queryId, result, true);
      } catch (final Exception e) {
        LOG.warn("Caching Web Search Results failed: " + e);
      }
      LOG.debug("\tQueryBing:" + (new Date().getTime() - start.getTime()));
    }

    return score(result, values);
  }


  /*
   * public static void main(String[] args) throws APIKeysDepletedException, IOException { String[]
   * accountKeys = new String[]{"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="}; WSScorer matcher =
   * new WSScorer( new HeaderWebsearchMatcherCache(
   * "D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr", "collection1"), new
   * BingSearch(accountKeys), new ArrayList<String>() ); matcher.computeElementScores(
   * "House of Cards", "Peter David"); matcher.computeElementScores("University of Sheffield",
   * "Sheffield", "United Kingdom"); matcher.computeElementScores("House of Cards", "Peter David");
   * matcher.cache.closeConnection(); }
   */

}
