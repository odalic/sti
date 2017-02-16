/**
 * 
 */
package cz.cuni.mff.xrg.odalic.files;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import cz.cuni.mff.xrg.odalic.files.formats.Format;
import cz.cuni.mff.xrg.odalic.tasks.Task;
import cz.cuni.mff.xrg.odalic.util.storage.DbService;

/**
 * This {@link FileService} implementation persists the files in {@link DB}-backed maps.
 * 
 * @author Václav Brodec
 *
 */
public final class DbFileService implements FileService {

  /**
   * The shared database instance.
   */
  private final DB db;

  /**
   * Table of files where the rows are indexed by user IDs and the columns by file IDs (represented
   * as an array of size 2).
   */
  private final BTreeMap<Object[], File> files;

  /**
   * Table of file content where the rows are indexed by user IDs and the columns by file URLs
   * (represented as an array of size 2).
   */
  private final BTreeMap<Object[], byte[]> data;

  /**
   * A multimap from user ID and file ID pairs to task IDs implemented as a map of user ID, file ID
   * and task ID triples to dummy boolean values.
   */
  private final BTreeMap<Object[], Boolean> utilizingTasks;

  /**
   * Creates the file service with no registered files and data.
   */
  @Autowired
  @SuppressWarnings("unchecked")
  public DbFileService(final DbService dbService) {
    Preconditions.checkNotNull(dbService);

    this.db = dbService.getDb();

    this.files = db.treeMap("files")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.JAVA).createOrOpen();
    this.data = db.treeMap("data")
        .keySerializer(new SerializerArrayTuple(Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.BYTE_ARRAY).createOrOpen();
    this.utilizingTasks = db.treeMap("utilizingTasks")
        .keySerializer(
            new SerializerArrayTuple(Serializer.STRING, Serializer.STRING, Serializer.STRING))
        .valueSerializer(Serializer.BOOLEAN).createOrOpen();
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#create(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void create(File file) {
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
  public void create(File file, InputStream fileInputStream) throws IOException {
    Preconditions.checkArgument(!existsFileWithId(file.getOwner().getEmail(), file.getId()));

    replace(file, fileInputStream);
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#deleteById(java.lang.String)
   */
  @Override
  public void deleteById(String userId, String fileId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);

    checkUtilization(userId, fileId);

    final File file = this.files.remove(new Object[] {userId, fileId});
    Preconditions.checkArgument(file != null);

    this.data.remove(new Object[] {userId, file.getLocation().toString()});

    db.commit();
  }

  private void checkUtilization(final String userId, final String fileId)
      throws IllegalStateException {
    final Set<String> utilizingTaskIds = utilizingTasks.prefixSubMap(new Object[] {userId, fileId})
        .keySet().stream().map(e -> (String) e[2]).collect(ImmutableSet.toImmutableSet());

    if (!utilizingTaskIds.isEmpty()) {
      final String jointUtilizingTasksIds = String.join(", ", utilizingTaskIds);
      throw new IllegalStateException(
          String.format("Some tasks (%s) still refer to this file!", jointUtilizingTasksIds));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getById(java.lang.String)
   */
  @Override
  public File getById(String userId, String fileId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);

    final File file = this.files.get(new Object[] {userId, fileId});
    Preconditions.checkArgument(file != null, "File does not exists!");

    return file;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#getFiles()
   */
  @Override
  public List<File> getFiles(String userId) {
    return ImmutableList.copyOf(this.files.prefixSubMap(new Object[] {userId}).values());
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#replace(cz.cuni.mff.xrg.odalic.files.File)
   */
  @Override
  public void replace(File file) {
    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    final Object[] userFileId = new Object[] {userId, fileId};

    final File previous = this.files.get(userFileId);
    if (previous != null && !previous.getLocation().equals(file.getLocation())) {
      this.data.remove(new Object[] {userId, previous.getLocation().toString()});
    }

    this.files.put(userFileId, file);

    db.commit();
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

    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    this.files.put(new Object[] {userId, fileId}, file);
    this.data.put(new Object[] {userId, file.getLocation().toString()},
        IOUtils.toByteArray(fileInputStream));

    db.commit();
  }

  /*
   * (non-Javadoc)
   * 
   * @see cz.cuni.mff.xrg.odalic.files.FileService#existsFileWithId(java.lang.String)
   */
  @Override
  public boolean existsFileWithId(String userId, String fileId) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);

    return this.files.containsKey(new Object[] {userId, fileId});
  }

  @Override
  public String getDataById(String userId, String fileId) throws IOException {
    final File file = getById(userId, fileId);

    final byte[] data = this.data.get(new Object[] {userId, file.getLocation().toString()});
    final Charset encoding = file.getFormat().getCharset();

    if (data == null) {
      return IOUtils.toString(file.getLocation(), encoding);
    } else {
      return new String(data, encoding);
    }
  }

  @Override
  public void subscribe(final File file, final Task task) {
    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    Preconditions.checkArgument(files.get(new Object[] {userId, fileId}).equals(file),
        "The file is not registered!");

    final Boolean previous = utilizingTasks.put(new Object[] {userId, fileId, task.getId()}, true);
    Preconditions.checkArgument(previous == null, "The task has already been subcscribed to the file!");
  }

  @Override
  public void unsubscribe(final File file, final Task task) {
    final String userId = file.getOwner().getEmail();
    final String fileId = file.getId();

    Preconditions.checkArgument(files.get(new Object[] {userId, fileId}).equals(file),
        "The file is not registered!");

    final Boolean removed = utilizingTasks.remove(new Object[] {userId, fileId, task.getId()});
    Preconditions.checkArgument(removed != null, "The task is not subcscribed to the file!");
  }

  @Override
  public Format getFormatForFileId(final String userId, final String fileId) {
    return getById(userId, fileId).getFormat();
  }

  @Override
  public void setFormatForFileId(final String userId, final String fileId, final Format format) {
    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(fileId);
    Preconditions.checkNotNull(format);

    final Object[] userFileId = new Object[] {userId, fileId};

    final File previousfile = this.files.get(userFileId);
    final File newFile = new File(previousfile.getOwner(), previousfile.getId(),
        previousfile.getUploaded(), previousfile.getLocation(), format, previousfile.isCached());

    this.files.put(userFileId, newFile);

    db.commit();
  }
}
