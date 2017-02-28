package uk.ac.shef.dcs.sti.nlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dragon.nlp.tool.lemmatiser.EngLemmatiser;

/**
 */
public class Lemmatizer {

  private EngLemmatiser lemmatizer;
  private final Map<String, Integer> tagLookUp = new HashMap<>();

  public Lemmatizer(final String dict) {
    init(dict);
  }

  public String getLemma(final String value, final String pos) {
    final int POS = this.tagLookUp.get(pos);
    if (POS == 0) {
      return this.lemmatizer.lemmatize(value);
    } else {
      return this.lemmatizer.lemmatize(value, POS);
    }
  }

  private void init(final String dict) {
    this.lemmatizer = new EngLemmatiser(dict, false, true);
    this.tagLookUp.put("NN", 1);
    this.tagLookUp.put("NNS", 1);
    this.tagLookUp.put("NNP", 1);
    this.tagLookUp.put("NNPS", 1);
    this.tagLookUp.put("VB", 2);
    this.tagLookUp.put("VBG", 2);
    this.tagLookUp.put("VBD", 2);
    this.tagLookUp.put("VBN", 2);
    this.tagLookUp.put("VBP", 2);
    this.tagLookUp.put("VBZ", 2);
    this.tagLookUp.put("JJ", 3);
    this.tagLookUp.put("JJR", 3);
    this.tagLookUp.put("JJS", 3);
    this.tagLookUp.put("RB", 4);
    this.tagLookUp.put("RBR", 4);
    this.tagLookUp.put("RBS", 4);
  }

  public List<String> lemmatize(final Collection<String> words) {
    final List<String> lemmas = new ArrayList<>();
    for (final String w : words) {
      final String lem = getLemma(w, "NN");
      if (lem.trim().length() < 1) {
        continue;
      }
      lemmas.add(lem);
    }
    return lemmas;
  }

}
