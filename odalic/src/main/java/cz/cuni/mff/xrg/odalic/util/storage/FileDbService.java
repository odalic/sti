/**
 * 
 */
package cz.cuni.mff.xrg.odalic.util.storage;

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
  
  private final DB db; 
  
  @Autowired
  public FileDbService(final PropertiesService propertiesService) {
    this(initializeDb(propertiesService));
  }

  private static DB initializeDb(final PropertiesService propertiesService) {
    Preconditions.checkNotNull(propertiesService);
    
    final Properties properties = propertiesService.get();
    final String fileName = properties.getProperty(FILE_NAME_PROPERTY_KEY);
    Preconditions.checkArgument(fileName != null, String.format("Missing key %s in the configuration!", FILE_NAME_PROPERTY_KEY));
    
    return DBMaker.fileDB(fileName).closeOnJvmShutdown().transactionEnable().make();
  }
  
  public FileDbService(final DB db) {
    Preconditions.checkNotNull(db);
    
    this.db = db;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.util.storage.DbService#getDb()
   */
  @Override
  public DB getDb() {
    return this.db;
  }
}
