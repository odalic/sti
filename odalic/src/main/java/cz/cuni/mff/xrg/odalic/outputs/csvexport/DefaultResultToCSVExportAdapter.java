package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.input.Input;
import cz.cuni.mff.xrg.odalic.input.ListsBackedInputBuilder;
import cz.cuni.mff.xrg.odalic.tasks.annotations.EntityCandidate;
import cz.cuni.mff.xrg.odalic.tasks.annotations.KnowledgeBase;
import cz.cuni.mff.xrg.odalic.tasks.configurations.Configuration;
import cz.cuni.mff.xrg.odalic.tasks.executions.KnowledgeBaseProxyFactory;
import cz.cuni.mff.xrg.odalic.tasks.results.Result;

/**
 * The default {@link ResultToCSVExportAdapter} implementation.
 * 
 * @author Josef Janoušek
 *
 */
public class DefaultResultToCSVExportAdapter implements ResultToCSVExportAdapter {

  private final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory;
  
  @Autowired
  public DefaultResultToCSVExportAdapter(final KnowledgeBaseProxyFactory knowledgeBaseProxyFactory) {
    Preconditions.checkNotNull(knowledgeBaseProxyFactory);
    
    this.knowledgeBaseProxyFactory = knowledgeBaseProxyFactory;
  }
  
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
    List<List<String>> primaries = new ArrayList<List<String>>();
    List<List<String>> alternatives = new ArrayList<List<String>>();
    for (int j = 0; j < input.rowsCount(); j++) {
      primaries.add(new ArrayList<String>());
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
              
              for (EntityCandidate chosen : entry.getValue()) {
                primaries.get(j).add(chosen.getEntity().getResource());
              }
            } else {
              addAlternatives = true;
              
              for (EntityCandidate chosen : entry.getValue()) {
                alternatives.get(j).add(chosen.getEntity().getResource());
              }
            }
          }
        }
      }
      
      if (addPrimary || addAlternatives) {
        builder.insertHeader(urlFormat(headers.get(i)), newPosition);
        
        for (int j = 0; j < input.rowsCount(); j++) {
          if (!primaries.get(j).isEmpty()) {
            builder.insertCell(primaries.get(j).remove(0), j, newPosition);
            primaries.get(j).clear();
          }
          else if (!alternatives.get(j).isEmpty()) {
            builder.insertCell(alternatives.get(j).remove(0), j, newPosition);
          }
          else {
            builder.insertCell(null, j, newPosition);
          }
        }
        
        newPosition++;
      }
      
      if (addAlternatives) {
        builder.insertHeader(alternativeUrlsFormat(headers.get(i)), newPosition);
        
        for (int j = 0; j < input.rowsCount(); j++) {
          if (!alternatives.get(j).isEmpty()) {
            builder.insertCell(StringUtils.join(alternatives.get(j), SEPARATOR), j, newPosition);
            alternatives.get(j).clear();
          }
          else {
            builder.insertCell(null, j, newPosition);
          }
        }
        
        newPosition++;
      }
    }
    
    if (configuration.isStatistical()) {
      final URI kbUri = knowledgeBaseProxyFactory.getKBProxies().get(configuration.getPrimaryBase().getName())
          .getKbDefinition().getInsertDataElementPrefix();
      
      builder.insertHeader(urlFormat(OBSERVATION), newPosition);
      
      for (int j = 0; j < input.rowsCount(); j++) {
        builder.insertCell(String.format("%sobservation/%s", kbUri, UUID.randomUUID()), j, newPosition);
      }
      
      newPosition++;
    }
    
    return builder.build();
  }
  
  private static final String SEPARATOR = " ";
  private static final String OBSERVATION = "OBSERVATION";
  
  private String urlFormat(String text) {
    return String.format("%s_url", text);
  }
  
  private String alternativeUrlsFormat(String text) {
    return String.format("%s_alternative_urls", text);
  }
}
