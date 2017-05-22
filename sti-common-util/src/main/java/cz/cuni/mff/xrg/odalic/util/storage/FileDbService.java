/**
 *
 */
package cz.cuni.mff.xrg.odalic.util.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
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

  private static final String ROOT_PATH_PROPERTY_KEY = "sti.home";
  private static final String FILE_PATH_PROPERTY_KEY = "cz.cuni.mff.xrg.odalic.db.file";

  private static DB initializeDb(final PropertiesService propertiesService) {
    Preconditions.checkNotNull(propertiesService);

    final Properties properties = propertiesService.get();
    
    final String filePath = properties.getProperty(FILE_PATH_PROPERTY_KEY);
    Preconditions.checkArgument(filePath != null,
        String.format("Missing key %s in the configuration!", FILE_PATH_PROPERTY_KEY));
    
    final String rootPathValue = properties.getProperty(ROOT_PATH_PROPERTY_KEY);
    Preconditions.checkArgument(rootPathValue != null,
        String.format("Missing key %s in the configuration!", ROOT_PATH_PROPERTY_KEY));
    
    final Path rootPath = Paths.get(rootPathValue);

    return DBMaker.fileDB(rootPath.resolve(filePath).toFile()).closeOnJvmShutdown().transactionEnable().make();
  }

  private final DB db;

  private FileDbService(final DB db) {
    Preconditions.checkNotNull(db);

    this.db = db;
  }

  @Autowired
  public FileDbService(final PropertiesService propertiesService) {
    this(initializeDb(propertiesService));
  }

  @Override
  public DB getDb() {
    return this.db;
  }
  
  public void cleanUp() throws Exception {
    this.db.close();
  }
}
