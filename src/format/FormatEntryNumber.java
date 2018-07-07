package format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import util.RomanNumeral;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import data.EntryNumber;

public class FormatEntryNumber
{
	private static List<Character> numberFormat = new ArrayList<Character>(
			Arrays.asList('r', 'R', 'a', 'L'));

	private String pattern;

	private Pattern regExp;
	private List<Character> groupToNumberFormat; // 'a' = arabic, 'r' = roman
	private List<Integer> groupToLevel;

	@JsonCreator
	public FormatEntryNumber(@JsonProperty("pattern") String pattern)
	{
		groupToNumberFormat = new ArrayList<Character>();
		groupToLevel = new ArrayList<Integer>();

		process(pattern);
	}

	public boolean setPattern(String pattern)
	{
		return process(pattern);
	}

	private boolean process(String numberPattern)
	{
		// number patter
		// #r0 = numbering of level 0 with lowercase roman numbers
		// #a1 = numbering of level 1 with arabic number
		// #R2 = numbering of level 2 with uppercase roman numbers
		// level 0 - 9 are allowed

		// temp var
		List<Character> tempGroupToNumberFormat = new ArrayList<Character>();
		List<Integer> tempGroupToLevel = new ArrayList<Integer>();
		// ArrayList<String> tempNumberPatternStrs = new ArrayList<String>();

		String newPattern = "";
		int prevIndex = 0;
		int curIndex = numberPattern.indexOf('#');
		while (curIndex != -1)
		{
			// build new pattern
			newPattern += numberPattern.substring(prevIndex, curIndex);
			// tempNumberPatternStrs.add(numberPattern.substring(prevIndex,
			// curIndex));

			// read
			if (curIndex + 2 >= numberPattern.length())
			{
				System.out.println("Error: unusual pattern: " + numberPattern);
				return false;
			}

			char curNumberFormat = numberPattern.charAt(curIndex + 1);
			if (!numberFormat.contains(curNumberFormat))
			{
				System.out.println("Error: unusual numberFormat: "
						+ curNumberFormat + " in pattern " + numberPattern);
				return false;
			}

			tempGroupToNumberFormat.add(curNumberFormat);

			char curLevel = numberPattern.charAt(curIndex + 2);
			if (!(curLevel >= '0' && curLevel <= '9'))
			{
				System.out.println("Error: unusual level: " + curLevel
						+ " in pattern " + numberPattern);
				return false;
			}
			int curLevelInt = Integer.parseInt(Character.toString(curLevel));

			if (tempGroupToLevel.contains(curLevelInt))
			{
				System.out.println("Error: level " + curLevel
						+ " occurs multiple times in pattern " + numberPattern);
				return false;
			}

			tempGroupToLevel.add(curLevelInt);

			// build new pattern
			prevIndex = curIndex + 3;
			if (curNumberFormat == 'r')
			{
				// newPattern += "([IVX]+)";
				newPattern += "([ivxIVX]+)"; // more robust
			} else if (curNumberFormat == 'a')
			{
				newPattern += "(\\d+)";
			} else if (curNumberFormat == 'R')
			{
				// newPattern += "([ivx]+)";
				newPattern += "([ivxIVX]+)"; // more robust
			} else if (curNumberFormat == 'L')
			{
				newPattern += "([A-Z])";
			}

			curIndex = numberPattern.indexOf('#', prevIndex);
		}

		newPattern += numberPattern
				.substring(prevIndex, numberPattern.length());
		// tempNumberPatternStrs.add(numberPattern.substring(prevIndex,
		// numberPattern.length()));

		newPattern = newPattern.replaceAll("\\s+", "\\\\s+"); // more robust
		if (newPattern.length() > 0)
		{
			newPattern = newPattern + "\\s"; // numbering must be separated
			// from
			// the title
		}

		try
		{
			this.regExp = Pattern.compile(newPattern);
		} catch (PatternSyntaxException e)
		{
			return false;
		}

		this.groupToNumberFormat = tempGroupToNumberFormat;
		this.groupToLevel = tempGroupToLevel;
		this.pattern = numberPattern;

		return true;
	}

	public EntryNumber match(String str, int level)
	{
		Matcher matcher = regExp.matcher(str);

		// boolean res = matcher.matches();
		boolean res = matcher.lookingAt();

		if (!res)
		{
			return null;
		}

		String orig = str.substring(0, matcher.end());
		EntryNumber en = new EntryNumber(/* this, */orig, level);

		// read entry numbering
		for (int i = 0; i < groupToLevel.size(); ++i)
		{
			String numStr = matcher.group(i + 1);

			if (null == numStr)
			{
				System.out.println("Error could not find group " + i);
				return null;
			}

			int num = 0;
			if (groupToNumberFormat.get(i) == 'r')
			{
				num = RomanNumeral.RomanStringToInt(numStr.toLowerCase());
			} else if (groupToNumberFormat.get(i) == 'R')
			{
				num = RomanNumeral.RomanStringToInt(numStr.toLowerCase());
			} else if (groupToNumberFormat.get(i) == 'a')
			{
				num = Integer.parseInt(numStr);
			} else if (groupToNumberFormat.get(i) == 'L')
			{
				num = (int) numStr.charAt(0) - (int) 'A'; // A=0, B=1, C=2...
			}

			en.setLevelToNumber(groupToLevel.get(i), num);

		}

		// TODO improve parsing: replace numbers

		// remove whitespace
		String[] array = en.getOrig().split("\\s+");
		StringBuilder sb = new StringBuilder(array[0]);
		for (int i = 1; i < array.length; ++i)
		{
			sb.append(" ");
			sb.append(array[i]);
		}
		en.setString(sb.toString());

		return en;
	}

	public String toString()
	{
		return pattern;
	}

	public String toRegExp()
	{
		return regExp.toString();
	}
}
