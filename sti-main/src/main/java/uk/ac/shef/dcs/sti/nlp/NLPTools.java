package uk.ac.shef.dcs.sti.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**

 */
public class NLPTools {
  private static NLPTools _ref;

  public static NLPTools getInstance(final String nlpResources) throws IOException {
    if (_ref == null) {
      _ref = new NLPTools(nlpResources);
    }
    return _ref;
  }

  private final POSTagger _posTagger;
  private final Chunker _npChunker;
  private final SentenceDetector _sentDetect;
  private final Tokenizer _tokenizer;

  private Lemmatizer _lemmatizer;

  private NLPTools(final String nlpResources) throws IOException {
    this._lemmatizer = new Lemmatizer(nlpResources + File.separator + "lemmatizer");
    final POSModel posModel =
        new POSModel(new FileInputStream(nlpResources + "/en-pos-maxent.bin"));
    this._posTagger = new POSTaggerME(posModel);

    final ChunkerModel chunkerModel =
        new ChunkerModel(new FileInputStream(nlpResources + "/en-chunker.bin"));
    this._npChunker = new ChunkerME(chunkerModel);

    final TokenizerModel tokenizerModel =
        new TokenizerModel(new FileInputStream(nlpResources + "/en-token.bin"));
    this._tokenizer = new TokenizerME(tokenizerModel);

    final SentenceModel sentModel =
        new SentenceModel(new FileInputStream(nlpResources + "/en-sent.bin"));
    this._sentDetect = new SentenceDetectorME(sentModel);
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  public Lemmatizer getLemmatizer() {
    return this._lemmatizer;
  }

  public Chunker getPhraseChunker() {
    return this._npChunker;
  }

  public POSTagger getPosTagger() {
    return this._posTagger;
  }

  public SentenceDetector getSentenceSplitter() {
    return this._sentDetect;
  }

  public Tokenizer getTokeniser() {
    return this._tokenizer;
  }

  public void setLemmatizer(final Lemmatizer _lemmatizer) {
    this._lemmatizer = _lemmatizer;
  }
}
