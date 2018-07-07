package components.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import format.FormatPageNumber;

public class NodeFormatPageNumber
{
	public FormatPageNumber fPageNumber;

	@JsonCreator 
	public NodeFormatPageNumber(@JsonProperty("fPageNumber") FormatPageNumber fPageNumber)
	{
		this.fPageNumber = fPageNumber;
	}

	public String toString()
	{
		return fPageNumber.toString();
	}

	public boolean setfPageNumber(String str)
	{
		try
		{
			fPageNumber = FormatPageNumber.valueOf(str);
		} catch (IllegalArgumentException e)
		{
			return false;
		}

		return true;
	}
}
