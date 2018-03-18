package uk.ac.shef.dcs.sti.core.subjectcol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import uk.ac.shef.dcs.sti.core.model.TContext;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.util.FileUtils;
import uk.ac.shef.dcs.util.Pair;

/**
 * produces matching statistics of a table's header and its contexts. can only computeElementScores
 * headers that are NOT STOPWORDS
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * the scoring algorithm is implemented in this class
 */
class CMScorer {

  private static final double MINIMUM_CONTEXT_SCORE = 0.5; // context blocks with an importance
                                                           // computeElementScores lower than this
                                                           // will not be considered
  private static final int MAXIMUM_CONTEXTS_TO_MATCH = 10; // except title, caption,
                                                           // computeElementScores a maximum of 5
                                                           // context blocks around the table
  private static final double SCALAR_MAJOR_CONTEXT_WEIGHT = 2.0; // major context blocks, i.e.,
                                                                 // titles, captions, plural word
                                                                 // matches
  // are considered more important. if matched, their scores is multiplied by this factor
  private final Lemmatizer lemmatizer;
  private List<String> stopwords;

  public CMScorer(final String nlpResource) throws IOException {
    this.lemmatizer = NLPTools.getInstance(nlpResource).getLemmatizer();
    try {
      this.stopwords = FileUtils.readList(nlpResource + File.separator + "stoplist.txt", false);
    } catch (final IOException e) {
      this.stopwords = new ArrayList<>();
    }

  }

  // returns a map between column index and matching computeElementScores
  @SuppressWarnings("unlikely-arg-type")
public Map<Integer, Double> score(final Table table, final int... col_indexes) {
    final Map<Integer, Double> scores = new HashMap<Integer, Double>();

    // learn headers to computeElementScores against
    final Map<Integer, List<String>> headerKeywords = new HashMap<>();
    for (final int col_id : col_indexes) {
      final List<String> searchWords = new ArrayList<>();

      final String keyword = table.getColumnHeader(col_id).getHeaderText();
      // searchWords.add(lemmatizer.getLemma(keyword, "NN"));

      // then add capitalised, noun-stop words
      final String[] candidates = keyword.split("\\s+");
      for (final String candidate : candidates) {
        String w = candidate;
        /*
         * if (StringUtils.isCapitalized(w)) { w = lemmatizer.getLemma(w, "NN"); if
         * (!stopwords.contains(w)) searchWords.add(w); }
         */
        // if (StringUtils.isCapitalized(w)) {
        w = this.lemmatizer.getLemma(w, "NN");
        if (!this.stopwords.contains(w)) {
          searchWords.add(w);
          // }
        }
      }
      headerKeywords.put(col_id, searchWords);
    }

    // List<String> stop = new ArrayList<String>(stopwords);
    // stop.removeAll(headerKeywords.values());

    // learn contexts to generate word lookup maps
    final List<TContext> contexts = table.getContexts();
    Collections.sort(contexts);

    int countContextBlocks = 0;
    double score = 0.0;
    for (final TContext ctx : contexts) {
      if (countContextBlocks == MAXIMUM_CONTEXTS_TO_MATCH) {
        break;
      }
      if (ctx.getImportanceScore() < MINIMUM_CONTEXT_SCORE) {
        continue;
      }

      // collect distinct words from this context, their frequency, and plural form frequency
      final Map<String, Pair<Integer, Integer>> wordFreq = new HashMap<>();
      final StringTokenizer tokenizer = new StringTokenizer(ctx.getText());
      while (tokenizer.hasMoreTokens()) {
        final String tok = tokenizer.nextToken();
        final String canonical = this.lemmatizer.getLemma(tok, "NN");

        Pair<Integer, Integer> countings = wordFreq.get(canonical);
        if (countings == null) {
          countings = new Pair<>(0, 0);
        }
        final int k = countings.getKey() + 1;
        int v = countings.getValue();
        if (!tok.toLowerCase().equals(canonical)) {
          v = v + 1;
        }
        countings = new Pair<>(k, v);

        wordFreq.put(canonical, countings);
      }

      // compute matching scores for each header keyword against this context block
      for (final Map.Entry<Integer, List<String>> headerKey : headerKeywords.entrySet()) {
        final List<String> words = headerKey.getValue();
        if (words == null) {
          continue;
        }

        // firstly lets try the full header text, i.e., element 1 in "words"
        for (final String word : words) {
          final Pair<Integer, Integer> freq = wordFreq.get(word);
          if (freq == null) {
            continue;
          }

          score = score + freq.getKey(); // if header keyword matches this word, its
                                         // computeElementScores is incremented by its frequency
          score = score + freq.getValue();// if the matched word is plural, the computeElementScores
                                          // is further modified
          if (ctx.getType().equals(TContext.TableContextType.CAPTION)
              || ctx.getType().equals(TContext.TableContextType.PAGETITLE)) {
            score = score * SCALAR_MAJOR_CONTEXT_WEIGHT;
          }
          Double prevScore = scores.get(headerKey.getKey());
          prevScore = prevScore == null ? 0 : prevScore;
          prevScore = prevScore + score;
          scores.put(headerKey.getKey(), prevScore);
        }
      }

      // special context blocks does not count towards the maximum number of context blocks to be
      // considered
      if (ctx.getType().equals(TContext.TableContextType.CAPTION)
          || ctx.getText().equals(TContext.TableContextType.PAGETITLE)) { // FIXME: Comparing String to Enum (maybe PAGETITLE.getText() was meant?)
      } else {
        countContextBlocks++;
      }
    }

    return scores;
  }


}
