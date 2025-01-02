package org.apache.ibatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public final class IOUtils {

  private IOUtils() {
  }

  public static void closeSilently(InputStream inputStream) {
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

  public static void closeSilently(Reader reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }
}
