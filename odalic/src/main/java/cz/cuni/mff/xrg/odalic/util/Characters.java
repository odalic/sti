/**
 *
 */
package cz.cuni.mff.xrg.odalic.util;

/**
 * Utility class for -- you guessed it -- working with characters.
 *
 * @author Václav Brodec
 *
 */
public final class Characters {

  /**
   * Carriage return character.
   */
  public static final char CARRIAGE_RETURN = '\r';

  /**
   * Line feed character.
   */
  public static final char LINE_FEED = '\n';

  /**
   * Indicates line-breaking character.
   *
   * @param character character
   * @return when the character is not null and is a line-breaking character
   */
  public static boolean isLineBreak(final Character character) {
    if (character == null) {
      return false;
    }

    final char value = character.charValue();
    return (value == LINE_FEED) || (value == CARRIAGE_RETURN);
  }

  private Characters() {}
}
