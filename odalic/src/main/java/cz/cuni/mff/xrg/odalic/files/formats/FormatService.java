package cz.cuni.mff.xrg.odalic.files.formats;

/**
 * Configuration service handles the CRUD operations for {@link Format} instances.
 * 
 * @author Václav Brodec
 *
 */
public interface FormatService {

  Format getForFileId(String fileId);

  void setForFileId(String fileId, Format format);
}
