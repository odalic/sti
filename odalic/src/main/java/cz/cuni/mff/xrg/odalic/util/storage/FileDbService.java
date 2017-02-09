/**
 * 
 */
package cz.cuni.mff.xrg.odalic.util.storage;

import java.util.Properties;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;
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
  
  private final Maker maker; 
  
  @Autowired
  public FileDbService(final PropertiesService propertiesService) {
    this(initializeMaker(propertiesService));
  }

  private static Maker initializeMaker(final PropertiesService propertiesService) {
    Preconditions.checkNotNull(propertiesService);
    
    final Properties properties = propertiesService.get();
    final String fileName = properties.getProperty(FILE_NAME_PROPERTY_KEY);
    Preconditions.checkArgument(fileName != null, String.format("Missing key %s in the configuration!", FILE_NAME_PROPERTY_KEY));
    
    return DBMaker.fileDB(fileName).closeOnJvmShutdown().transactionEnable();
  }
  
  public FileDbService(final Maker maker) {
    Preconditions.checkNotNull(maker);
    
    this.maker = maker;
  }

  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.util.storage.DbService#get()
   */
  @Override
  public DB get() {
    return this.maker.make();
  }
}
