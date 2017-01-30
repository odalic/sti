package cz.cuni.mff.xrg.odalic.files.formats;

/**
 * Configuration service handles the CRUD operations for {@link Format} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface FormatService {

  /**
   * Gets the file format.
   * 
   * @param userId user ID
   * @param fileId file ID
   * 
   * @return CSV file format
   */
  Format getForFileId(String userId, String fileId);

  /**
   * Sets the file format.
   * 
   * @param userId user ID
   * @param fileId file ID
   * @param format CSV file format
   */
  void setForFileId(String userId, String fileId, Format format);
}
