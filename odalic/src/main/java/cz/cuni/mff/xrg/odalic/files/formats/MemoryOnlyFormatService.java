package cz.cuni.mff.xrg.odalic.files.formats;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;

import cz.cuni.mff.xrg.odalic.files.File;
import cz.cuni.mff.xrg.odalic.files.FileService;

/**
 * This {@link FormatService} implementation provides no persistence.
 * 
 * @author Václav Brodec
 *
 */
public final class MemoryOnlyFormatService implements FormatService {

  private final FileService fileService;
  
  /* (non-Javadoc)
   * @see cz.cuni.mff.xrg.odalic.tasks.configurations.ConfigurationService#getForFileId(java.lang.String)
   */
  @Override
  public Format getForFileId(String fileId) {
    final File file = fileService.getById(fileId);
    
    return file.getFormat();
  }

  /**
   * @param fileId
   * @param configuration
   */
  @Override
  public void setForFileId(String fileId, Format configuration) {
    final File file = fileService.getById(fileId);
    
    file.setFormat(configuration);
  }
  
  @Autowired
  public MemoryOnlyFormatService(FileService taskService) {
    Preconditions.checkNotNull(taskService);
    
    this.fileService = taskService;
  }
}
