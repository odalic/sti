package cz.cuni.mff.xrg.odalic.outputs.csvexport;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import cz.cuni.mff.xrg.odalic.input.Input;

/**
 * Service providing the extended CSV (a part of the task result when using standard table
 * annotations).
 * 
 * @author Václav Brodec
 *
 */
public interface CsvExportService {
  /**
   * Gets a part of the task result in the form of extended CSV.
   * @param userId user ID
   * @param taskId task ID
   * 
   * @return extended CSV output
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   * @throws IOException if an I/O exception occurs when creating the output content
   */
  String getExtendedCsvForTaskId(String userId, String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException;

  /**
   * Gets a part of the task result in the form of extended input.
   * @param userId user ID
   * @param taskId task ID
   * 
   * @return extended input
   * @throws InterruptedException if the execution was interrupted while waiting
   * @throws ExecutionException if the computation threw an exception
   * @throws CancellationException if the computation was cancelled
   * @throws IOException if an I/O exception occurs when creating the output content
   */
  Input getExtendedInputForTaskId(String userId, String taskId)
      throws CancellationException, InterruptedException, ExecutionException, IOException;
}
