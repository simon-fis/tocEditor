package format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import components.tree.NodeFormatPageNumber;

public class FormatLevel
{
	private int level;
	private NodeFormatPageNumber nodefPageNumber;
	private FormatEntryNumber fEntryNumber;

	@JsonCreator
	public FormatLevel(@JsonProperty("level") int level,
			@JsonProperty("pattern") String pattern,
			@JsonProperty("fPageNumber") FormatPageNumber fPageNumber)
	{
		this.level = level;
		this.fEntryNumber = new FormatEntryNumber(pattern);
		this.nodefPageNumber = new NodeFormatPageNumber(fPageNumber);
	}

	public int getLevel()
	{
		return level;
	}

	public FormatPageNumber getfPageNumber()
	{
		return nodefPageNumber.fPageNumber;
	}
	
	public String getPattern(){
		return fEntryNumber.toString();
	}

	@JsonIgnore
	public FormatEntryNumber getfEntryNumber()
	{
		return fEntryNumber;
	}

	@JsonIgnore
	public NodeFormatPageNumber getNodefPageNumber()
	{
		return nodefPageNumber;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public boolean setfEntryNumber(String newValue)
	{
		return fEntryNumber.setPattern(newValue);// processNumberPattern(level, newValue);
	}

	public String toFormatString()
	{
		return "level:" + level + " nodefPageNumber:" + nodefPageNumber + " pattern:" + fEntryNumber;
	}

	public String toString()
	{
		return "level:" + level;
	}
}
