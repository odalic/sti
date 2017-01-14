/**
 * 
 */
package cz.cuni.mff.xrg.odalic.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * This {@link FileService} implementation provides no persistence.
 * 
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFileService implements FileService {

  private final Map<String, File> files;

  private final Map<URL, byte[]> data;

  private final Multimap<String, String> utilizingTasks;

  private MemoryOnlyFileService(Map<String, File> files, Map<URL, byte[]> data,
      Multimap<String, String> utilizingTasks) {
    Preconditions.checkNotNull(files);
    Preconditions.checkNotNull(data);

    this.files = files;
    this.data = data;
    this.utilizingTasks = utilizingTasks;
  }

  /**
   * Creates the file service with no registered files and data.
   */
  public MemoryOnlyFileService() {
    this(new HashMap<>(), new HashMap<>(), HashMultimap.create());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void create(File file) {
    Preconditions.checkArgument(!existsFileWithId(file.getId()));

    replace(file);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File,
   * java.io.InputStream)
   */
  @Override
  public void create(File file, InputStream fileInputStream) throws IOException {
    Preconditions.checkArgument(!existsFileWithId(file.getId()));

    replace(file, fileInputStream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#deleteById(java.lang.String)
   */
  @Override
  public void deleteById(String id) {
    Preconditions.checkNotNull(id);

    checkUtilization(id);

    final File file = this.files.remove(id);
    Preconditions.checkArgument(file != null);

    this.data.remove(file.getLocation());
  }

  private void checkUtilization(final String fileId) throws IllegalStateException {
    final Collection<String> utilizingTaskIds = utilizingTasks.get(fileId);
    
    final String jointUtilizingTasksIds = String.join(", ", utilizingTaskIds);
    
    Preconditions.checkState(utilizingTaskIds.isEmpty(),
        String.format("Some tasks (%s) still refer to this file!", jointUtilizingTasksIds));
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getById(java.lang.String)
   */
  @Override
  public File getById(String id) {
    Preconditions.checkNotNull(id);

    final File file = this.files.get(id);
    Preconditions.checkArgument(file != null, "File does not exists!");

    return file;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getFiles()
   */
  @Override
  public List<File> getFiles() {
    return ImmutableList.copyOf(this.files.values());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void replace(File file) {
    final File previous = this.files.get(file.getId());
    if (previous != null && !previous.getLocation().equals(file.getLocation())) {
      this.data.remove(previous.getLocation());
    }

    this.files.put(file.getId(), file);
  }


  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File,
   * java.io.InputStream)
   */
  @Override
  public void replace(File file, InputStream fileInputStream) throws IOException {
    Preconditions.checkArgument(file.isCached());

    this.files.put(file.getId(), file);
    this.data.put(file.getLocation(), IOUtils.toByteArray(fileInputStream));
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#existsFileWithId(java.lang.String)
   */
  @Override
  public boolean existsFileWithId(String id) {
    Preconditions.checkNotNull(id);

    return this.files.containsKey(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#hasId(cz.cuni.mff.xrg.odalic.files.File,
   * java.lang.String)
   */
  @Override
  public boolean hasId(File file, String id) {
    Preconditions.checkNotNull(id);

    return file.getId().equals(id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getDataById(java.lang.String)
   */
  @Override
  public String getDataById(String id) throws IOException {
    final File file = getById(id);

    final byte[] data = this.data.get(file.getLocation());
    final Charset encoding = file.getFormat().getCharset();

    if (data == null) {
      return IOUtils.toString(file.getLocation(), encoding);
    } else {
      return new String(data, encoding);
    }
  }

  @Override
  public void subscribe(final File file, final Task task) {
    final String fileId = file.getId();

    Preconditions.checkArgument(files.get(fileId).equals(file), "The file is not registered!");

    final boolean inserted = utilizingTasks.put(fileId, task.getId());
    Preconditions.checkArgument(inserted, "The task has already been subcscribed to the file!");
  }

  @Override
  public void unsubscribe(final File file, final Task task) {
    final String fileId = file.getId();

    Preconditions.checkArgument(files.get(fileId).equals(file), "The file is not registered!");

    final boolean removed = utilizingTasks.remove(fileId, task.getId());
    Preconditions.checkArgument(removed, "The task is not subcscribed to the file!");
  }
}
