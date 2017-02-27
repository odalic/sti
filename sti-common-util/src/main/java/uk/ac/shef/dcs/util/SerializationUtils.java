package uk.ac.shef.dcs.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 07/05/13 Time: 16:20
 */
@SuppressWarnings("restriction")
public class SerializationUtils {

  public static Object deserializeBase64(final byte[] bytes)
      throws IOException, ClassNotFoundException {
    final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    final ObjectInputStream is = new ObjectInputStream(in);
    return is.readObject();
  }

  public static String serializeBase64(final Object obj) throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ObjectOutputStream os = new ObjectOutputStream(out);
    os.writeObject(obj);
    return Base64.encode(out.toByteArray()); // This method is too important to change the
                                             // implementation during re-factoring. At least for
                                             // now.
  }
}
