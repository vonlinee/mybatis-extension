package org.mybatis.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class StringUtils {

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
   * Capitalizes all the delimiter separated words in a String.
   * Only the first character of each word is changed. To convert the
   * rest of each word to lowercase at the same time,
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
   * WordUtils.capitalize(null, *)            = null
   * WordUtils.capitalize("", *)              = ""
   * WordUtils.capitalize(*, new char[0])     = *
   * WordUtils.capitalize("i am fine", null)  = "I Am Fine"
   * WordUtils.capitalize("i aM.fine", {'.'}) = "I aM.Fine"
   * WordUtils.capitalize("i am fine", new char[]{}) = "I am fine"
   * </pre>
   *
   * @param str        the String to capitalize, may be null
   * @param delimiters set of characters to determine capitalization, null means whitespace
   * @return capitalized String, {@code null} if null String input
   * @see #uncapitalize(String)
   */
  public static String capitalize(final String str, final char... delimiters) {
    if (StringUtils.isEmpty(str)) {
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
   * Uncapitalizes all the whitespace separated words in a String.
   * Only the first character of each word is changed.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   * A {@code null} input String returns {@code null}.</p>
   *
   * <pre>
   * WordUtils.uncapitalize(null)        = null
   * WordUtils.uncapitalize("")          = ""
   * WordUtils.uncapitalize("I Am FINE") = "i am fINE"
   * </pre>
   *
   * @param str the String to uncapitalize, may be null
   * @return uncapitalized String, {@code null} if null String input
   * @see #capitalize(String)
   */
  public static String uncapitalize(final String str) {
    return uncapitalize(str, null);
  }

  /**
   * Capitalizes all the whitespace separated words in a String.
   * Only the first character of each word is changed.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   * A {@code null} input String returns {@code null}.
   * Capitalization uses the Unicode title case, normally equivalent to
   * upper case.</p>
   *
   * <pre>
   * WordUtils.capitalize(null)        = null
   * WordUtils.capitalize("")          = ""
   * WordUtils.capitalize("i am FINE") = "I Am FINE"
   * </pre>
   *
   * @param str the String to capitalize, may be null
   * @return capitalized String, {@code null} if null String input
   * @see #uncapitalize(String)
   */
  public static String capitalize(final String str) {
    return capitalize(str, null);
  }

  /**
   * un-capitalizes all the whitespace separated words in a String.
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
   * WordUtils.uncapitalize(null, *)            = null
   * WordUtils.uncapitalize("", *)              = ""
   * WordUtils.uncapitalize(*, null)            = *
   * WordUtils.uncapitalize(*, new char[0])     = *
   * WordUtils.uncapitalize("I AM.FINE", {'.'}) = "i AM.fINE"
   * WordUtils.uncapitalize("I am fine", new char[]{}) = "i am fine"
   * </pre>
   *
   * @param str        the String to uncapitalize, may be null
   * @param delimiters set of characters to determine uncapitalization, null means whitespace
   * @return uncapitalized String, {@code null} if null String input
   * @see #capitalize(String)
   */
  public static String uncapitalize(final String str, final char... delimiters) {
    if (isEmpty(str)) {
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

  public static String[] trimSplitToArray(String str) {
    return trimSplitToArray(str, ",");
  }

  public static String[] trimSplitToArray(String str, String separator) {
    String[] split = str.split(separator);
    for (int i = 0; i < split.length; i++) {
      split[i] = split[i].trim();
    }
    return split;
  }
}
