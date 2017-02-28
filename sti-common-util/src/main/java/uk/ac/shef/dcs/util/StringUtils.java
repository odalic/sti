package uk.ac.shef.dcs.util;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk) Date: 30/04/13 Time: 18:30
 */
public class StringUtils {
  private static Pattern ORDINAL_NUMBER_PATTERN = Pattern.compile("(?<=[0-9])(?:st|nd|rd|th)");

  public static String combinePaths(final String basePath, final String relativePath) {
    if (Paths.get(relativePath).isAbsolute()) {
      return relativePath;
    } else {
      return Paths.get(basePath, relativePath).toString();
    }
  }

  public static boolean isCapitalized(final String string) {
    return (Character.isUpperCase(string.charAt(0)));
  }

  // permitted tokens must be numeric, or ordinal
  public static boolean isNumericArray(final String[] alphanums) {
    int countNumerics = 0, firstNumeric = -1;
    for (int index = 0; index < alphanums.length; index++) {
      if (isNumericToken(alphanums[index]) || isOrdinalNumber(alphanums[index])) {
        countNumerics++;
        if (index == 0) {
          firstNumeric = 1;
        }
      }

    }
    if ((countNumerics >= (alphanums.length - countNumerics)) && (firstNumeric == 1)) {
      return true;
    }

    // otherwise, we have less or equal numeric tokens than other tokens, check special cases as:
    // if multi tokens, the first token must be a number, then it must be followed by 0-2 tokens,
    // each must not exceed a length of 10 characters. these wors are likely to be "units", e.g.,
    // "kilometers"
    if (firstNumeric == 1) {
      if (alphanums.length > 3) {
        return false; // because it is likely to be an address. However this cannot perfect
                      // distinguish true/false positives
      }
      for (int i = 1; i < alphanums.length; i++) {
        if (alphanums[i].length() > 10) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  public static boolean isNumericToken(final String alphanum) {
    int legal_letters = 0, illegal_letters = 0;
    for (int i = 0; i < alphanum.length(); i++) { // allowing scientific numbers
      if (!Character.isDigit(alphanum.charAt(i))) {
        if ((alphanum.charAt(i) == 'e') || (alphanum.charAt(i) == 'E')
            || (alphanum.charAt(i) == 'x') || (alphanum.charAt(i) == 'X')) {
          legal_letters++;
        } else {
          illegal_letters++;
        }
      }
    }

    if ((illegal_letters == 0) && (legal_letters < 2)) {
      return true;
    }

    // in this case there are letters in the token.
    final String alphabeticRemoved = alphanum.replaceAll("[^\\d]", " ").trim();
    return !alphabeticRemoved.contains(" ") // it means either the alphabetics are suffix or prefix
                                            // and are trimmed, or there are no alhpabetics
        && (alphabeticRemoved.length() > (alphanum.length() - alphabeticRemoved.length()));

  }


  public static boolean isOrdinalNumber(final String singleToken) {
    return ORDINAL_NUMBER_PATTERN.matcher(singleToken).find();
  }

  public static boolean isPath(String value) {
    if (value.startsWith("http://") || value.startsWith("www.") || value.startsWith("ftp://")
        || value.startsWith("https://")) {
      return true;
    }
    value = value.replaceAll("\\\\", "/");
    final int slashes = value.split("/").length;
    return ((slashes - 1) > 2) && !value.contains(" ");
  }


  public static String splitCamelCase(final String s) {
    return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
  }

  public static List<String> splitToAlphaNumericTokens(final String value,
      final boolean lowercase) {
    final List<String> query_tokens = new ArrayList<>();
    for (final String t : toAlphaNumericWhitechar(value).split("\\s+")) {
      if (t.length() == 0) {
        continue;
      }
      if (lowercase) {
        query_tokens.add(t.toLowerCase());
      } else {
        query_tokens.add(t);
      }
    }
    return query_tokens;
  }

  // convert a string to only alphenumeric and white chars. any other chars are removed
  public static String toAlphaNumericWhitechar(final String value) {
    // keep only alpha-numeric characters
    return value.replaceAll("[^\\p{L}\\s\\d]", " ");

  }

  public static List<String> toBagOfWords(String text, final boolean lowercase,
      final boolean alphanumeric, final boolean discard_single_char) {
    final List<String> rs = new ArrayList<>();
    if (lowercase) {
      text = text.toLowerCase();
    }
    if (alphanumeric) {
      text = toAlphaNumericWhitechar(text);
    }
    for (String vp : text.split("\\s+")) {
      vp = vp.trim();
      if (discard_single_char && (vp.length() > 1)) {
        rs.add(vp);
      } else if (!discard_single_char && (vp.length() > 0)) {
        rs.add(vp);
      }
    }
    return rs;
  }
}
