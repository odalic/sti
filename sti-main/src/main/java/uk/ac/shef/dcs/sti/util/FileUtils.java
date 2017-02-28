package uk.ac.shef.dcs.sti.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.LineIterator;

/**
 * Several utility methods related to files.
 *
 * @author <a href="mailto:z.zhang@dcs.shef.ac.uk">Ziqi Zhang</a>
 */
public class FileUtils {


  /**
   * Read input raw text file as a list
   *
   * @param path input file path
   * @param lowercase whether to convert input string to lowercase
   * @return
   * @throws IOException
   */
  public static List<String> readList(final String path, final boolean lowercase)
      throws IOException {
    final List<String> res = new ArrayList<>();
    final LineIterator it = org.apache.commons.io.FileUtils.lineIterator(new File(path));
    while (it.hasNext()) {
      final String line = it.nextLine().trim();
      if (line.equals("")) {
        continue;
      }
      if (lowercase) {
        res.add(line.toLowerCase());
      } else {
        res.add(line);
      }
    }

    return res;
  }

  public static List<String> readList(final String path, final boolean lowercase,
      final String charset) throws IOException {
    final List<String> res = new ArrayList<>();
    final InputStreamReader ir = new InputStreamReader(new FileInputStream(path), charset);

    final BufferedReader reader = new BufferedReader(ir);
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.equals("")) {
        continue;
      }
      if (lowercase) {
        res.add(line.toLowerCase());
      } else {
        res.add(line);
      }
    }

    reader.close();
    return res;
  }

}

