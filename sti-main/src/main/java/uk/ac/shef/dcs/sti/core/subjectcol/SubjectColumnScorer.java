package uk.ac.shef.dcs.sti.core.subjectcol;

import java.util.List;
import java.util.Map;

import uk.ac.shef.dcs.util.Pair;

/**
 * Created by - on 18/03/2016.
 */
abstract class SubjectColumnScorer {

  protected abstract Map<Integer, Pair<Double, Boolean>> score(
      List<TColumnFeature> featuresOfNEColumns);
}
