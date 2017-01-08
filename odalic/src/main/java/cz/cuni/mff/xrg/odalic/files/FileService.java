package cz.cuni.mff.xrg.odalic.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * Provides basic set of operations for uploaded or remote files.
 * 
 * @author Václav Brodec
 *
 */
public interface FileService {

  /**
   * Registers a new remote file.
   * 
   * @param file file description
   */
  void create(File file);

  /**
   * Registers a new file and reads its content from the stream.
   * 
   * @param file file description
   * @param fileInputStream file content
   * @throws IOException if an I/O error occurs
   */
  void create(File file, InputStream fileInputStream) throws IOException;

  /**
   * @param id file ID
   * @throws IllegalStateException when some {@link Task} utilizes the file
   */
  void deleteById(String id);

  File getById(String id);

  List<File> getFiles();

  /**
   * Replaces file description.
   * 
   * @param file file description
   */
  void replace(File file);

  /**
   * Replaces the file description and the file content.
   * 
   * @param file file description
   * @param fileInputStream file content
   * @throws IOException if an I/O error occurs
   */
  void replace(File file, InputStream fileInputStream) throws IOException;

  boolean existsFileWithId(String id);

  boolean hasId(File file, String id);

  /**
   * Reads content of the file.
   * 
   * @param id file ID
   * @return textual content of the file
   * @throws IOException if an I/O error occurs
   */
  String getDataById(String id) throws IOException;

  /**
   * Subscribe the task for utilizing the file.
   * 
   * @param file utilized file
   * @param task utilizing task
   */
  void subscribe(File input, Task task);

  /**
   * Unsubscribe the task from utilizing the file.
   * 
   * @param file utilized file
   * @param task utilizing task
   */
  void unsubscribe(File file, Task task);
}
