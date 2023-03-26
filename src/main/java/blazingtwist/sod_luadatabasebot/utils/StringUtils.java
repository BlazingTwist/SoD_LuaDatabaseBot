package blazingtwist.sod_luadatabasebot.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class StringUtils {

	public enum SplitCharRule {
		/**
		 * The split-character will not be present in any of the substrings
		 */
		Remove,

		/**
		 * The split-character will be appended to each substring
		 */
		Append,

		/**
		 * The split-character will be prepended to each substring.
		 */
		Prepend
	}

	/**
	 * Split a String on a specified set of characters,
	 * such that each substring is at most maxLength characters long.
	 *
	 * <p>Substrings that are longer than the maxLength, but do not contain a split character, will not be split.</p>
	 *
	 * @param str        the string to split
	 * @param maxLength  max length of substrings
	 * @param splitRule  how to handle split characters
	 * @param splitChars set of characters to split on
	 * @return a list of substrings
	 */
	public static List<String> SplitWithMaxLength(String str, int maxLength, SplitCharRule splitRule, char... splitChars) {
		if (str.length() <= maxLength) {
			return Collections.singletonList(str);
		}

		HashSet<Character> splitCharSet = new HashSet<>();
		for (char splitChar : splitChars) {
			splitCharSet.add(splitChar);
		}

		char[] chars = str.toCharArray();
		List<String> subStrings = new ArrayList<>();
		int lastSplitCharIndex = -1;
		boolean foundSplitChar = false;
		int currentWordLength = 0;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (foundSplitChar && currentWordLength > maxLength) {
				foundSplitChar = false;
				int wordStartIndex = i - currentWordLength;
				int wordLen = currentWordLength - (i - lastSplitCharIndex);
				subStrings.add(new String(chars, wordStartIndex, splitRule == SplitCharRule.Append ? wordLen + 1 : wordLen));
				currentWordLength -= splitRule == SplitCharRule.Prepend ? wordLen : wordLen + 1;
			}

			if (splitCharSet.contains(c)) {
				lastSplitCharIndex = i;
				foundSplitChar = true;
			}
			currentWordLength++;
		}
		if (currentWordLength > 0) {
			subStrings.add(new String(chars, chars.length - currentWordLength, currentWordLength));
		}
		return subStrings;
	}

	public static boolean isNullOrWhitespace(String str) {
		return str == null || str.isBlank();
	}
}
