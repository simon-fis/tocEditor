package format;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import components.tree.NodeInteger;
import components.tree.NodeFormatPageNumber;

public class FormatPart
{
	private int part;
	private NodeInteger startEntry;
	private NodeFormatPageNumber nodefPageNumber;
	public List<FormatLevel> fLevels;

	public FormatPart()
	{
		this(FormatPageNumber.arabic);
	}
	
	public FormatPart(FormatPageNumber fPageNumber)
	{
		this(-1,-1,fPageNumber);
	}

	@JsonCreator
	public FormatPart(@JsonProperty("part") int part,
			@JsonProperty("startEntry") int startEntry,
			@JsonProperty("fPageNumber") FormatPageNumber fPageNumber)
	{
		this.part = part;
		this.startEntry = new NodeInteger(startEntry);
		this.nodefPageNumber = new NodeFormatPageNumber(fPageNumber);
		this.fLevels = new ArrayList<FormatLevel>();
	}

	public void removefLevel(int level)
	{
		fLevels.remove(level);

		adaptfLevels();
	}

	public void addfLevel(int index, FormatLevel fLevel)
	{
		if (index < 0 || index > fLevels.size())
		{
			System.out.println("index out of bounds " + index);
			return;
		}

		if (fLevel == null)
		{
			System.out.println("can not add null FormatLevel");
			return;
		}

		fLevels.add(index, fLevel);

		adaptfLevels();
	}

	public void addfLevel(FormatLevel fLevel)
	{
		addfLevel(fLevels.size(), fLevel);
	}

	private void adaptfLevels()
	{
		for (int i = 0; i < fLevels.size(); ++i)
		{

			if (fLevels.get(i).getLevel() != i)
			{
				System.out.println("adapted level " + i);
				fLevels.get(i).setLevel(i);
			}
		}
	}

	public int getPart()
	{
		return part;
	}

	public int getStartEntry()
	{
		return startEntry.val;
	}

	public FormatPageNumber getfPageNumber()
	{
		return nodefPageNumber.fPageNumber;
	}

	public void setPart(int part)
	{
		this.part = part;
	}

	public void setStartEntry(int val)
	{
		startEntry.val = val;
	}

	@JsonIgnore
	public NodeInteger getStartEntryNode()
	{
		return startEntry;
	}
	
	@JsonIgnore
	public NodeFormatPageNumber getNodefPageNumber()
	{
		return nodefPageNumber;
	}

	@JsonIgnore
	public boolean isGood()
	{
		boolean good = true;
	
		for (int i = 0; i < fLevels.size(); ++i)
		{
			boolean found = false;
			for (FormatLevel fLevel : fLevels)
			{
				if (fLevel.getLevel() == i)
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				good = false;
				System.out.println("level " + i + " is missing");
			}
		}
		return good;
	}

	public String toFormatString()
	{
		String str = "part:" + part;
		str += " startEntry:" + startEntry.toString();
		str += " fPageNumber:" + ((nodefPageNumber != null) ? nodefPageNumber.toString() : "");
		str += "\n";
		for (FormatLevel fLevel : fLevels)
		{
			str += "  " + fLevel.toFormatString() + "\n";
		}
		return str;
	}

	public String toString()
	{
		return "part:" + part;
	}
}
