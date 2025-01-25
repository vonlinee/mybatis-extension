package org.apache.ibatis.util;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Predicate;

public final class StringUtils {

  /**
   * Checks if a CharSequence is empty (""), null or whitespace only.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
   *
   * <pre>
   * StringUtils.isBlank(null)      = true
   * StringUtils.isBlank("")        = true
   * StringUtils.isBlank(" ")       = true
   * StringUtils.isBlank("bob")     = false
   * StringUtils.isBlank("  bob  ") = false
   * </pre>
   *
   * @param cs the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is null, empty or whitespace only
   * @since 2.0
   * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
   */
  public static boolean isBlank(final CharSequence cs) {
    if (cs == null || cs.length() == 0) {
      return true;
    }
    final int strLen = cs.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if a CharSequence is empty ("") or null.
   *
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   *
   * <p>NOTE: This method changed in Lang version 2.0.
   * It no longer trims the CharSequence.
   * That functionality is available in isBlank().</p>
   *
   * @param cs the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is empty or null
   * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
   */
  public static boolean isEmpty(final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  /**
   * Un-capitalizes all the whitespace separated words in a String.
   * Only the first character of each word is changed.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   * A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.uncapitalize(null)        = null
   * StringUtils.uncapitalize("")          = ""
   * StringUtils.uncapitalize("I Am FINE") = "i am fINE"
   * </pre>
   *
   * @param str the String to uncapitalize, may be null
   * @return uncapitalized String, {@code null} if null String input
   * @see #capitalize(String)
   */
  public static String uncapitalize(final String str) {
    return uncapitalize(str, (char[]) null);
  }

  /**
   * Uncapitalizes all the whitespace separated words in a String.
   * Only the first character of each word is changed.
   *
   * <p>The delimiters represent a set of characters understood to separate words.
   * The first string character and the first non-delimiter character after a
   * delimiter will be uncapitalized.</p>
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   * A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * StringUtils.uncapitalize(null, *)            = null
   * StringUtils.uncapitalize("", *)              = ""
   * StringUtils.uncapitalize(*, null)            = *
   * StringUtils.uncapitalize(*, new char[0])     = *
   * StringUtils.uncapitalize("I AM.FINE", {'.'}) = "i AM.fINE"
   * StringUtils.uncapitalize("I am fine", new char[]{}) = "i am fine"
   * </pre>
   *
   * @param str        the String to uncapitalize, may be null
   * @param delimiters set of characters to determine uncapitalization, null means whitespace
   * @return uncapitalized String, {@code null} if null String input
   * @see #capitalize(String)
   */
  public static String uncapitalize(final String str, final char... delimiters) {
    if (StringUtils.isEmpty(str)) {
      return str;
    }
    final Predicate<Integer> isDelimiter = generateIsDelimiterFunction(delimiters);
    final int strLen = str.length();
    final int[] newCodePoints = new int[strLen];
    int outOffset = 0;

    boolean uncapitalizeNext = true;
    for (int index = 0; index < strLen; ) {
      final int codePoint = str.codePointAt(index);

      if (isDelimiter.test(codePoint)) {
        uncapitalizeNext = true;
        newCodePoints[outOffset++] = codePoint;
        index += Character.charCount(codePoint);
      } else if (uncapitalizeNext) {
        final int titleCaseCodePoint = Character.toLowerCase(codePoint);
        newCodePoints[outOffset++] = titleCaseCodePoint;
        index += Character.charCount(titleCaseCodePoint);
        uncapitalizeNext = false;
      } else {
        newCodePoints[outOffset++] = codePoint;
        index += Character.charCount(codePoint);
      }
    }
    return new String(newCodePoints, 0, outOffset);
  }

  /**
   * Converts all the whitespace separated words in a String into capitalized words,
   * that is each word is made up of a title-case character and then a series of
   * lowercase characters.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   * A {@code null} input String returns {@code null}.
   * Capitalization uses the Unicode title case, normally equivalent to
   * upper case.</p>
   *
   * <pre>
   * StringUtils.capitalizeFully(null)        = null
   * StringUtils.capitalizeFully("")          = ""
   * StringUtils.capitalizeFully("i am FINE") = "I Am Fine"
   * </pre>
   *
   * @param str the String to capitalize, may be null
   * @return capitalized String, {@code null} if null String input
   */
  public static String capitalizeFully(final String str) {
    return capitalizeFully(str, (char[]) null);
  }

  /**
   * Converts all the delimiter separated words in a String into capitalized words,
   * that is each word is made up of a title case character and then a series of
   * lowercase characters.
   *
   * <p>The delimiters represent a set of characters understood to separate words.
   * The first string character and the first non-delimiter character after a
   * delimiter will be capitalized.</p>
   *
   * <p>A {@code null} input String returns {@code null}.
   * Capitalization uses the Unicode title case, normally equivalent to
   * upper case.</p>
   *
   * <pre>
   * StringUtils.capitalizeFully(null, *)            = null
   * StringUtils.capitalizeFully("", *)              = ""
   * StringUtils.capitalizeFully(*, null)            = *
   * StringUtils.capitalizeFully(*, new char[0])     = *
   * StringUtils.capitalizeFully("i aM.fine", {'.'}) = "I am.Fine"
   * </pre>
   *
   * @param str        the String to capitalize, may be null
   * @param delimiters set of characters to determine capitalization, null means whitespace
   * @return capitalized String, {@code null} if null String input
   */
  public static String capitalizeFully(String str, final char... delimiters) {
    if (isEmpty(str)) {
      return str;
    }
    str = str.toLowerCase();
    return capitalize(str, delimiters);
  }

  /**
   * Capitalizes all the whitespace separated words in a String.
   * Only the first character of each word is changed. To convert the
   * rest of each word to lowercase at the same time,
   * use {@link #capitalizeFully(String)}.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   * A {@code null} input String returns {@code null}.
   * Capitalization uses the Unicode title case, normally equivalent to
   * upper case.</p>
   *
   * <pre>
   * StringUtils.capitalize(null)        = null
   * StringUtils.capitalize("")          = ""
   * StringUtils.capitalize("i am FINE") = "I Am FINE"
   * </pre>
   *
   * @param str the String to capitalize, may be null
   * @return capitalized String, {@code null} if null String input
   * @see #uncapitalize(String)
   * @see #capitalizeFully(String)
   */
  public static String capitalize(final String str) {
    return capitalize(str, (char[]) null);
  }

  /**
   * Capitalizes all the delimiter separated words in a String.
   * Only the first character of each word is changed. To convert the
   * rest of each word to lowercase at the same time,
   * use {@link #capitalizeFully(String, char[])}.
   *
   * <p>The delimiters represent a set of characters understood to separate words.
   * The first string character and the first non-delimiter character after a
   * delimiter will be capitalized.</p>
   *
   * <p>A {@code null} input String returns {@code null}.
   * Capitalization uses the Unicode title case, normally equivalent to
   * upper case.</p>
   *
   * <pre>
   * StringUtils.capitalize(null, *)            = null
   * StringUtils.capitalize("", *)              = ""
   * StringUtils.capitalize(*, new char[0])     = *
   * StringUtils.capitalize("i am fine", null)  = "I Am Fine"
   * StringUtils.capitalize("i aM.fine", {'.'}) = "I aM.Fine"
   * StringUtils.capitalize("i am fine", new char[]{}) = "I am fine"
   * </pre>
   *
   * @param str        the String to capitalize, may be null
   * @param delimiters set of characters to determine capitalization, null means whitespace
   * @return capitalized String, {@code null} if null String input
   */
  public static String capitalize(final String str, final char... delimiters) {
    if (isEmpty(str)) {
      return str;
    }
    final Predicate<Integer> isDelimiter = generateIsDelimiterFunction(delimiters);
    final int strLen = str.length();
    final int[] newCodePoints = new int[strLen];
    int outOffset = 0;

    boolean capitalizeNext = true;
    for (int index = 0; index < strLen; ) {
      final int codePoint = str.codePointAt(index);

      if (isDelimiter.test(codePoint)) {
        capitalizeNext = true;
        newCodePoints[outOffset++] = codePoint;
        index += Character.charCount(codePoint);
      } else if (capitalizeNext) {
        final int titleCaseCodePoint = Character.toTitleCase(codePoint);
        newCodePoints[outOffset++] = titleCaseCodePoint;
        index += Character.charCount(titleCaseCodePoint);
        capitalizeNext = false;
      } else {
        newCodePoints[outOffset++] = codePoint;
        index += Character.charCount(codePoint);
      }
    }
    return new String(newCodePoints, 0, outOffset);
  }

  /**
   * Given the array of delimiters supplied; returns a function determining whether a character code point is a delimiter.
   * The function provides O(1) lookup time.
   * Whitespace is defined by {@link Character#isWhitespace(char)} and is used as the defaultvalue if delimiters is null.
   *
   * @param delimiters set of characters to determine delimiters, null means whitespace
   * @return Predicate<Integer> taking a code point value as an argument and returning true if a delimiter.
   */
  private static Predicate<Integer> generateIsDelimiterFunction(final char[] delimiters) {
    final Predicate<Integer> isDelimiter;
    if (delimiters == null || delimiters.length == 0) {
      isDelimiter = delimiters == null ? Character::isWhitespace : c -> false;
    } else {
      final Set<Integer> delimiterSet = new HashSet<>();
      for (int index = 0; index < delimiters.length; index++) {
        delimiterSet.add(Character.codePointAt(delimiters, index));
      }
      isDelimiter = delimiterSet::contains;
    }

    return isDelimiter;
  }

  /**
   * Removes extra whitespaces from a given string.
   * This method eliminates leading, trailing, and multiple consecutive spaces,
   * returning a string with single spaces between words.
   *
   * <p>Example usage:</p>
   * <pre>
   * String originalString = "   This   is  a   test   string.  ";
   * String cleanedString = removeExtraWhitespaces(originalString);
   * System.out.println(cleanedString); // Outputs: 'This is a test string.'
   * </pre>
   *
   * @param original the original string from which to remove extra whitespaces
   * @return a new string with extra whitespaces removed
   */
  public static String removeExtraWhitespaces(String original) {
    StringTokenizer tokenizer = new StringTokenizer(original);
    StringBuilder builder = new StringBuilder();
    boolean hasMoreTokens = tokenizer.hasMoreTokens();
    while (hasMoreTokens) {
      builder.append(tokenizer.nextToken());
      hasMoreTokens = tokenizer.hasMoreTokens();
      if (hasMoreTokens) {
        builder.append(' ');
      }
    }
    return builder.toString();
  }

  /**
   * Removes leading whitespace characters from the given string.
   * <p>
   * If the input string is null, this method returns an empty string.
   * Leading whitespace is defined as any character that is considered
   * whitespace according to the Unicode standard, including spaces,
   * tabs, and other whitespace characters.
   * </p>
   *
   * @param str the input string to be trimmed
   * @return a new string with leading whitespace removed;
   * returns an empty string if the input is null
   */
  public static String leftTrim(String str) {
    if (str == null) {
      return "";
    }
    int len = str.length();
    int st = 0;
    char[] val = str.toCharArray();
    while ((st < len) && (val[st] <= ' ')) {
      st++;
    }
    while ((st < len) && (val[len - 1] <= ' ')) {
      len--;
    }
    return ((st > 0)) ? str.substring(st, len) : str;
  }

  /**
   * Case in-sensitive find of the first index within a CharSequence
   * from the specified position.
   *
   * <p>A {@code null} CharSequence will return {@code -1}.
   * A negative start position is treated as zero.
   * An empty ("") search CharSequence always matches.
   * A start position greater than the string length only matches
   * an empty search CharSequence.</p>
   *
   * @param str       the CharSequence to check, may be null
   * @param searchStr the CharSequences to find, may be null
   * @return the first index of the search CharSequence (always &ge; startPos),
   * -1 if no match or {@code null} string input
   * @see StringUtils#indexOfIgnoreCase(String, String, int)
   */
  public static int indexOfIgnoreCase(final String str, final String searchStr) {
    return indexOfIgnoreCase(str, searchStr, 0);
  }

  /**
   * Case in-sensitive find of the first index within a CharSequence
   * from the specified position.
   *
   * <p>A {@code null} CharSequence will return {@code -1}.
   * A negative start position is treated as zero.
   * An empty ("") search CharSequence always matches.
   * A start position greater than the string length only matches
   * an empty search CharSequence.</p>
   *
   * @param str       the CharSequence to check, may be null
   * @param searchStr the CharSequences to find, may be null
   * @param startPos  the start position, negative treated as zero
   * @return the first index of the search CharSequence (always &ge; startPos),
   * -1 if no match or {@code null} string input
   */
  public static int indexOfIgnoreCase(final String str, final String searchStr, int startPos) {
    if (str == null || searchStr == null) {
      return -1;
    }
    if (startPos < 0) {
      startPos = 0;
    }
    final int endLimit = str.length() - searchStr.length() + 1;
    if (startPos > endLimit) {
      return -1;
    }
    if (searchStr.isEmpty()) {
      return startPos;
    }
    for (int i = startPos; i < endLimit; i++) {
      if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
        return i;
      }
    }
    return -1;
  }
}
