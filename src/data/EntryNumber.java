package data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EntryNumber
{
	private String orig;
	private String str;
	private int level;
	private Map<Integer, Integer> levelToNumber;

	public EntryNumber(String orig, int level)
	{
		this.orig = orig;
		this.level = level;
		this.levelToNumber = new HashMap<Integer, Integer>();
	}

	public boolean isDirectSuccessorOf(EntryNumber prevEn)
	{
		if (prevEn == null)
		{
			return false;
		}

		for (Entry<Integer, Integer> prevLevelValuePair : prevEn.levelToNumber.entrySet())
		{
			int levelToCheck = prevLevelValuePair.getKey();
			int prevValue = prevLevelValuePair.getValue();

			Integer nextValue = this.levelToNumber.get(levelToCheck);
			if (nextValue != null)
			{
				// both contain information about the level "levelToCheck"
				// this and prevEn are just allowed to differ in their in the value of
				// this.level and preEn.level

				if (levelToCheck != prevEn.level && levelToCheck != this.level)
				{
					if (nextValue != prevValue)
					{
						return false;
					}
					continue;
				}

				// In the following levelToCheck == this.level or levelToCheck == prevEn.level
				
				if (this.level <= prevEn.level)
				{
					// --> levelToCheck == this.level
					if (nextValue != prevValue + 1)
					{
						return false;
					}
				} else // this.level > prevEn.level
				{
					// --> levelToCheck == prevEn.level
					if (nextValue != prevValue)
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	public String toString()
	{
		return "String: " + str + "\nNumbers: " + levelToNumber.toString();
	}

	public String toFormatedString()
	{
		return str;
	}

	public void setLevelToNumber(int level, int number)
	{
		if (number < 0)
		{
			System.out.println("Error level=" + level + " number=" + number);
		}
		levelToNumber.put(level, number);
	}

	public int getLTNSize()
	{
		return levelToNumber.size();
	}

	public String getOrig()
	{
		return orig;
	}

	public void setString(String str)
	{
		this.str = str;
	}
}
