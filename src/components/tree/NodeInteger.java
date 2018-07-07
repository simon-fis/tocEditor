package components.tree;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeInteger
{
	public int val;

	@JsonCreator
	public NodeInteger(@JsonProperty("val") int val)
	{
		this.val = val;
	}

	public String toString()
	{
		return Integer.toString(val);
	}

	public boolean setValue(String str)
	{
		try
		{
			val = Integer.parseInt(str);
		} catch (NumberFormatException e)
		{
			return false;
		}
		return true;
	}
}
