/**
 *
 */
package cz.cuni.mff.xrg.odalic.files;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.tasks.Task;

/**
 * This {@link FileService} implementation provides no persistence.
 *
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFileService implements FileService {

  /**
   * Table of files where the rows are indexed by user IDs and the columns by file IDs.
   */
  private final Table<String, String, File> files;

  /**
   * Table of file content where the rows are indexed by user IDs and the columns by file URLs.
   */
  private final Table<String, URL, byte[]> data;

  /**
   * Table of task IDs (that belong to tasks that utilize the file indexed by the same row and
   * column) where the rows are indexed by user IDs and the columns by file IDs.
   */
  private final Table<String, String, Set<String>> utilizingTasks;

  /**
   * Creates the file service with no registered files and data.
   */
  public MemoryOnlyFileService() {
    this(HashBasedTable.create(), HashBasedTable.create(), HashBasedTable.create());
  }

  private MemoryOnlyFileService(final Table<String, String, File> files,
      final Table<String, URL, byte[]> data,
      final Table<String, String, Set<String>> utilizingTasks) {
    Preconditions.checkNotNull(files);
    Preconditions.checkNotNull(data);
    Preconditions.checkNotNull(utilizingTasks);

    this.files = files;
    this.data = data;
    this.utilizingTasks = utilizingTasks;
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void create(final File file) {
    Preconditions.checkArgument(!existsFileWithId(file.getOwner().getEmail(), file.getId()));

    replace(file);
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File,
   * java.io.InputStream)
   */
  @Override
  public void create(final File file, final InputStream fileInputStream) throws IOException {
    Preconditions.checkArgument(!existsFileWithId(file.getOwner().getEmail(), file.getId()));

    replace(file, fileInputStream);
  }

  @Override
  public void deleteAll(final String userId) {
    Preconditions.checkNotNull(userId);

    final Map<String, File> fileIdsToFiles = this.files.row(userId);
    fileIdsToFiles.entrySet().stream().forEach(e -> checkUtilization(userId, e.getValue().getId()));
    fileIdsToFiles.clear();

    this.data.row(userId).clear();
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#deleteById(java.lang.String)
   */
  @Override
  public void deleteById(final String userId, final String fileId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);

    checkUtilization(userId, fileId);

    final File file = this.files.remove(userId, fileId);
    Preconditions.checkArgument(file != null);

    this.data.remove(userId, file.getLocation());
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#existsFileWithId(java.lang.String)
   */
  @Override
  public boolean existsFileWithId(final String userId, final String fileId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);

    return this.files.contains(userId, fileId);
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getById(java.lang.String)
   */
  @Override
  public File getById(final String userId, final String fileId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);

    final File file = this.files.get(userId, fileId);
    Preconditions.checkArgument(file != null, "File does not exists!");

    return file;
  }

  @Override
  public String getDataById(final String userId, final String fileId) throws IOException {
    final File file = getById(userId, fileId);

    final byte[] data = this.data.get(userId, file.getLocation());
    final Charset encoding = file.getFormat().getCharset();

    if (data == null) {
      return IOUtils.toString(file.getLocation(), encoding);
    } else {
      return new String(data, encoding);
    }
  }


  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getFiles()
   */
  @Override
  public List<File> getFiles(final String userId) {
    return ImmutableList.copyOf(this.files.row(userId).values());
  }

  @Override
  public Format getFormatForFileId(final String userId, final String fileId) {
    return getById(userId, fileId).getFormat();
  }

  private void checkUtilization(final String userId, final String fileId)
      throws IllegalStateException {
    final Set<String> utilizingTaskIds = this.utilizingTasks.get(userId, fileId);
    if (utilizingTaskIds == null) {
      return;
    }

    final String jointUtilizingTasksIds = String.join(", ", utilizingTaskIds);
    Preconditions.checkState(utilizingTaskIds.isEmpty(),
        String.format("Some tasks (%s) still refer to this file!", jointUtilizingTasksIds));
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void replace(final File file) {
    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    final File previous = this.files.get(userId, fileId);
    if ((previous != null) && !previous.getLocation().equals(file.getLocation())) {
      this.data.remove(userId, previous.getLocation());
    }

    this.files.put(userId, fileId, file);
  }

  /*
   * (non-Javadoc)
   *
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File,
   * java.io.InputStream)
   */
  @Override
  public void replace(final File file, final InputStream fileInputStream) throws IOException {
    Preconditions.checkArgument(file.isCached());

    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    this.files.put(userId, fileId, file);
    this.data.put(userId, file.getLocation(), IOUtils.toByteArray(fileInputStream));
  }

  @Override
  public void setFormatForFileId(final String userId, final String fileId, final Format format) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);
    Preconditions.checkNotNull(format);

    final File previousfile = this.files.get(userId, fileId);
    final File newFile = new File(previousfile.getOwner(), previousfile.getId(),
        previousfile.getUploaded(), previousfile.getLocation(), format, previousfile.isCached());

    this.files.put(userId, fileId, newFile);
  }

  @Override
  public void subscribe(final File file, final Task task) {
    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    Preconditions.checkArgument(this.files.get(userId, fileId).equals(file),
        "The file is not registered!");

    final Set<String> tasks = this.utilizingTasks.get(userId, fileId);

    final boolean inserted;
    if (tasks == null) {
      this.utilizingTasks.put(userId, fileId, Sets.newHashSet(task.getId()));
      inserted = true;
    } else {
      inserted = tasks.add(task.getId());
    }

    Preconditions.checkArgument(inserted, "The task has already been subcscribed to the file!");
  }

  @Override
  public void unsubscribe(final File file, final Task task) {
    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    Preconditions.checkArgument(this.files.get(userId, fileId).equals(file),
        "The file is not registered!");

    final Set<String> tasks = this.utilizingTasks.get(userId, fileId);

    final boolean removed;
    if (tasks == null) {
      removed = false;
    } else {
      removed = tasks.remove(task.getId());

      if (tasks.isEmpty()) {
        this.utilizingTasks.remove(userId, fileId);
      }
    }

    Preconditions.checkArgument(removed, "The task is not subcscribed to the file!");
  }
}
