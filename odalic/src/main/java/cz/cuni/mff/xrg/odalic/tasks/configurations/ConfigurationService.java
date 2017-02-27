package cz.cuni.mff.xrg.odalic.tasks.configurations;

/**
 * Configuration service handles the CRUD operations for {@link Configuration} instances.
 *
 * @author Václav Brodec
 *
 */
public interface ConfigurationService {

  Configuration getForTaskId(String userId, String taskId);

  void setForTaskId(String userId, String taskId, Configuration execution);
}
