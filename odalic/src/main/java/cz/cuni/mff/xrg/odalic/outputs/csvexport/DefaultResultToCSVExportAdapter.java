package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * The default {@link ResultToCSVExportAdapter} implementation.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultResultToCSVExportAdapter implements ResultToCSVExportAdapter {

  /**
   * The default toCSVExport implementation.
   * 
   * @see cz.cuni.mff.xrg.odalic.outputs.csvexport.ResultToCSVExportAdapter#toCSVExport(cz.cuni.mff.xrg.odalic.results.Result, cz.cuni.mff.xrg.odalic.input.Input, cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration)
   */
  @Override
  public Input toCSVExport(Result result, Input input, Configuration configuration) {
    
    ListsBackedInputBuilder builder = new ListsBackedInputBuilder(input);
    List<String> headers = input.headers();
    int newPosition = input.columnsCount();
    List<List<String>> alternatives = new ArrayList<List<String>>();
    for (int j = 0; j < input.rowsCount(); j++) {
      alternatives.add(new ArrayList<String>());
    }
    
    for (int i = 0; i < input.columnsCount(); i++) {
      boolean addPrimary = false;
      boolean addAlternatives = false;
      
      for (int j = 0; j < input.rowsCount(); j++) {
        for (Entry<KnowledgeBase, Set<EntityCandidate>> entry : result.getCellAnnotations()[j][i].getChosen().entrySet()) {
          if (entry.getValue() != null && !entry.getValue().isEmpty()) {
            if (entry.getKey().getName().equals(configuration.getPrimaryBase().getName())) {
              addPrimary = true;
            } else {
              addAlternatives = true;
              
              for (EntityCandidate chosen : entry.getValue()) {
                alternatives.get(j).add(chosen.getEntity().getResource());
              }
            }
          }
        }
      }
      
      if (addPrimary) {
        builder.insertHeader(urlFormat(headers.get(i)), newPosition);
        
        for (int j = 0; j < input.rowsCount(); j++) {
          for (EntityCandidate chosen : result.getCellAnnotations()[j][i].getChosen().get(configuration.getPrimaryBase())) {
            builder.insertCell(chosen.getEntity().getResource(), j, newPosition);
          }
        }
        
        newPosition++;
      }
      
      if (addAlternatives) {
        builder.insertHeader(alternativeUrlsFormat(headers.get(i)), newPosition);
        
        for (int j = 0; j < input.rowsCount(); j++) {
          builder.insertCell(StringUtils.join(alternatives.get(j), SEPARATOR), j, newPosition);
          alternatives.get(j).clear();
        }
        
        newPosition++;
      }
    }
    
    return builder.build();
  }
  
  private static final String SEPARATOR = " ";
  
  private String urlFormat(String text) {
    return String.format("%s_url", text);
  }
  
  private String alternativeUrlsFormat(String text) {
    return String.format("%s_alternative_urls", text);
  }
}
