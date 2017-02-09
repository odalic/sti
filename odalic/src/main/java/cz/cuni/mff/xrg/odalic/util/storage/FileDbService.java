/**
 * 
 */
package cz.cuni.mff.xrg.odalic.util.storage;

import java.io.File;
import java.util.Properties;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.util.configuration.PropertiesService;

/**
 * File based {@link DbService}.
 * 
 * @author Václav Brodec
 *
 */
public final class FileDbService implements DbService {

  private static final String FILE_NAME_PROPERTY_KEY = "cz.cuni.mff.xrg.odalic.db.file";
  
  private final File file; 
  
  @Autowired
  public FileDbService(final PropertiesService propertiesService) {
    this(initializeFile(propertiesService));
  }

  private static File initializeFile(final PropertiesService propertiesService) {
    Preconditions.checkNotNull(propertiesService);
    
    final Properties properties = propertiesService.get();
    final String fileName = properties.getProperty(FILE_NAME_PROPERTY_KEY);
    Preconditions.checkArgument(fileName != null, String.format("Missing key %s in the configuration!", FILE_NAME_PROPERTY_KEY));
    
    return new File(fileName);
  }
  
  public FileDbService(final File file) {
    Preconditions.checkNotNull(file);
    
    Preconditions.checkArgument(file.exists());
    Preconditions.checkArgument(file.isFile());
    Preconditions.checkArgument(file.canRead());
    Preconditions.checkArgument(file.canWrite());
    
    this.file = file;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.util.storage.DbService#get()
   */
  @Override
  public DB get() {
    return DBMaker.newFileDB(file).closeOnJvmShutdown().make();
  }

}
